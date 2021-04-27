
#include <ArduinoJson.h>
#include <ESP8266WiFi.h>
#include <SimpleTimer.h>
#include "DHT.h"
#include <string.h>
#include <stdlib.h>
#include <FirebaseESP8266.h>

#define FIREBASE_HOST "https://fbtest-ad83f-default-rtdb.firebaseio.com/"
#define FIREBASE_AUTH "4MQ70rdeDLZjlTDuDWXcx4cvmMBp4Qi81zwN5k5V"
#define WIFI_SSID "Comvis"
#define WIFI_PASSWORD "12345654321"

FirebaseData fbdo;

// Cảm biến
#define DHTPIN D3     // Chân DATA nối với D3
#define SOIL_MOIST_1_PIN A0 // Chân A0 nối với cảm biến độ ẩm
// Relay, nút nhấn
#define PUMP_ON_BUTTON D0
#define LAMP_ON_BUTTON D1
#define ON_OFF_AUTO_BUTTON D4

#define PUMP_PIN D6   //Bom
#define LAMP_PIN D7   //Den
// Uncomment loại cảm biến bạn sử dụng
#define DHTTYPE DHT11   // DHT 11
//#define DHTTYPE DHT22   // DHT 22  (AM2302), AM2321
//#define DHTTYPE DHT21   // DHT 21 (AM2301)

/* Thông số cho chế độ tự động */
#define DRY_SOIL      10
#define WET_SOIL      30
#define COLD_TEMP     24
#define HOT_TEMP      26
#define TIME_PUMP_ON  5
#define TIME_LAMP_ON  5
/* TIMER */
#define READ_BUTTONS_TM   1L  // Tương ứng với giây
//#define READ_SOIL_TEMP_TM 2L
#define READ_SOIL_HUM_TM  10L //Đọc cảm biến ẩm đất
#define READ_AIR_DATA_TM  2L  //Đọc DHT
#define DISPLAY_DATA_TM   5L
#define SEND_UP_DATA_TM   10L //Giao tiếp platform
#define SET_DEVICE_TM     1L
#define AUTO_CTRL_TM      30L //Chế độ tư động

// Biến lưu các giá trị cảm biến
float humDHT = 0;
float tempDHT = 0;
//int lumen;
int soilMoist = 0;
// Biến lưu trạng thái điều khiển
boolean pumpStatus = 0;
boolean lampStatus = 0;
boolean autoStatus = 0;


int timePumpOn = 10; // Thời gian bật bơm nước
// Biến cho timer
long sampleTimingSeconds = 50; // ==> Thời gian đọc cảm biến (s)
long startTiming = 0;
long elapsedTime = 0;
// Khởi tạo timer
SimpleTimer timer;
// Khởi tạo cảm biến
DHT dht(DHTPIN, DHTTYPE);

/****************************************************************
  Khởi tạo
****************************************************************/
void setup() {
  pinMode(PUMP_PIN, OUTPUT);
  pinMode(LAMP_PIN, OUTPUT);
  pinMode(PUMP_ON_BUTTON, INPUT_PULLUP);
  pinMode(LAMP_ON_BUTTON, INPUT_PULLUP);
  pinMode(ON_OFF_AUTO_BUTTON, INPUT_PULLUP);
  aplyCmd();
  // put your setup code here, to run once:
  Serial.begin(115200);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  dht.begin();
  startTimers();

}

void loop() {
  timer.run(); // Chạy SimpleTimer

}
void getSoilMoist(void)
{
  int i = 0;
  soilMoist = 0;
  for (i = 0; i < 10; i++)  //
  {
    soilMoist += analogRead(SOIL_MOIST_1_PIN); //Đọc giá trị cảm biến độ ẩm đất
    delay(50);   // Đợi đọc giá trị ADC
  }

  soilMoist = soilMoist / (i);
  soilMoist = map(soilMoist, 1023, 0, 0, 100); //Ít nước:0%  ==> Nhiều nước 100%
}

void getDhtData(void)
{

  tempDHT = dht.readTemperature();
  humDHT = dht.readHumidity();
  if (isnan(humDHT) || isnan(tempDHT))   // Kiểm tra kết nối lỗi thì thông báo.
  {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }
}
void printData(void)
{
  // IN thông tin ra màn hình
  Serial.print("Do am: ");
  Serial.print(humDHT);
  Serial.print(" %\t");
  Serial.print("Nhiet do: ");
  Serial.print(tempDHT);
  Serial.print(" *C\t");
  Serial.print(" %\t");
  Serial.print("Do am dat: ");
  Serial.print(soilMoist);
  Serial.println(" %");
}

/****************************************************************
  Hàm đọc trạng thái bơm và kiểm tra nút nhấn
  (Nút nhấn mặc định là mức "CAO"):
****************************************************************/
void readLocalCmd()
{
  boolean digiValue = debounce(PUMP_ON_BUTTON);
  if (!digiValue)
  {
    pumpStatus = !pumpStatus;
    aplyCmd();
    sendUptime();
  }

  digiValue = debounce(LAMP_ON_BUTTON);
  if (!digiValue)
  {
    lampStatus = !lampStatus;
    aplyCmd();
    sendUptime();
  }

  digiValue = debounce(ON_OFF_AUTO_BUTTON);
  if (!digiValue)
  {
    autoStatus =  !autoStatus;
    sendUptime();
    Serial.println(" Change auto status ");
  }
}
/***************************************************
  Thực hiện điều khiển
****************************************************/
void aplyCmd()
{
  if (pumpStatus == 0) digitalWrite(PUMP_PIN, LOW);
  if (pumpStatus == 1) digitalWrite(PUMP_PIN, HIGH);

  if (lampStatus == 0) digitalWrite(LAMP_PIN, LOW);
  if (lampStatus == 1) digitalWrite(LAMP_PIN, HIGH);
}
/***************************************************
  Hàm kiểm tra trạng thái phím bấm
****************************************************/
boolean debounce(int pin)
{
  boolean state;
  boolean previousState;
  const int debounceDelay = 60;

  previousState = digitalRead(pin);
  for (int counter = 0; counter < debounceDelay; counter++)
  {
    delay(1);
    state = digitalRead(pin);
    if (state != previousState)
    {
      counter = 0;
      previousState = state;
    }
  }
  return state;
}
/***************************************************
  Chế độ tự động dựa trên thông số cảm biến
****************************************************/
void autoControlPlantation(void)
{

  if (autoStatus == 1) {
    if (soilMoist < DRY_SOIL)
    {
      pumpStatus = 1;
      aplyCmd();
    }
    if (soilMoist > WET_SOIL)
    {
      pumpStatus = 0;
      aplyCmd();
    }
    if (tempDHT < COLD_TEMP)
    {
      lampStatus = 1;
      aplyCmd();
    }
    if (tempDHT > HOT_TEMP)
    {
      lampStatus = 0;
      aplyCmd();
    }
    sendUptime();
  }

}
void setDevice() {
  String deviceData;
  if (Firebase.getString(fbdo, "/device")) {
    deviceData = fbdo.stringData();
    Serial.print("Device Status:");
    Serial.println(deviceData);
  } else {
    Serial.println(fbdo.errorReason());
  }
  


  if (deviceData.charAt(0) == '1') {
    pumpStatus = 1;
    aplyCmd();
  } else {
    pumpStatus = 0;
    aplyCmd();
  }
  if (deviceData.charAt(1) == '1') {
    lampStatus = 1;
    aplyCmd();
  } else {
    lampStatus = 0;
    aplyCmd();
  }
  if (deviceData.charAt(2) == '1') {
    autoStatus = 1;
  } else {
    autoStatus = 0;
  }

}







/***************************************************
   Giao tiếp với platform
 **************************************************/
void sendUptime()
{
  //Gửi độ ẩm
  if (Firebase.setFloat(fbdo, "/humDHT", humDHT)) {
    Serial.println("Success send humDHT");
  } else {
    Serial.println(fbdo.errorReason());
  }

  //Gửi nhiệt độ
  if (Firebase.setFloat(fbdo, "/tempDHT", tempDHT)) {
    Serial.println("Success send tempDHT");
  } else {
    Serial.println(fbdo.errorReason());
  }

  //Gửi độ ẩm đất
  if (Firebase.setInt(fbdo, "/soilMoist", soilMoist)) {
    Serial.println("Success send soilMoist");
  } else {
    Serial.println(fbdo.errorReason());
  }

  //Gửi thông tin thiết bị
  String devi = (String)(pumpStatus) + (String)(lampStatus) + (String)(autoStatus);
  if (Firebase.setString(fbdo, "/device", devi)) {
    Serial.println("Success send device info");
  } else {
    Serial.println(fbdo.errorReason());
  }

}

/***************************************************
  Khởi động Timers
****************************************************/
void startTimers(void)
{
  timer.setInterval(READ_BUTTONS_TM * 200, readLocalCmd);
  timer.setInterval(READ_AIR_DATA_TM * 1000, getDhtData);
  timer.setInterval(READ_SOIL_HUM_TM * 1000, getSoilMoist);

  timer.setInterval(AUTO_CTRL_TM * 1000, autoControlPlantation);

  timer.setInterval(SEND_UP_DATA_TM * 1000, sendUptime);
  timer.setInterval(SET_DEVICE_TM * 200, setDevice);
  timer.setInterval(DISPLAY_DATA_TM * 1000, printData);
}
