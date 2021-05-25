import mysql.connector
import pyrebase
import time
import matplotlib.pyplot as plt

#initializing
config = {
	"apiKey": "AIzaSyCKWp_L1KUxG0gMMdnHz9s6PQRtjbjjNoA",
    "authDomain": "fbtest-ad83f.firebaseapp.com",
    "databaseURL": "https://fbtest-ad83f-default-rtdb.firebaseio.com",
    "projectId": "fbtest-ad83f",
    "storageBucket": "fbtest-ad83f.appspot.com"
}

firebase = pyrebase.initialize_app(config)
db = firebase.database()
storage = firebase.storage()


mydb = mysql.connector.connect(
  host="localhost",
  user="admin",
  password="123456789",
  database="project3"
)
mycursor = mydb.cursor()
def getData():
	users = db.child("humDHT").get()
	_humDHT = users.val()
	users = db.child("soilMoist").get()
	_soilMoist = users.val()
	users = db.child("tempDHT").get()
	_tempDHT = users.val()


	sql = "INSERT INTO root_data(humDHT, soilMoist, tempDHT) VALUES (%s, %s, %s)"
	val = (_humDHT, _soilMoist, _tempDHT)
	mycursor.execute(sql, val)
	mydb.commit()
	print("get data complete")
	print(mycursor.rowcount, "record inserted.")
def drawFig():
	sql1 = "select * from root_data where created_at > date_sub(now(), interval 1 week ) LIMIT 10"

	mycursor.execute(sql1)

	myresult = mycursor.fetchall()
	date = []
	dataHum = []
	dataSoil = []
	dataTemp = []
	for x in myresult:
		date.append(x[1])
		dataHum.append(x[2])
		dataSoil.append(x[3])
		dataTemp.append(x[4])
	
	plt.figure(figsize=(20,5))
	plt.title("Humidity")
	plt.xlabel("time")
	plt.ylabel("%")
	plt.plot(date,dataHum,'o-')
	plt.savefig('humidity.png')

	plt.figure(figsize=(20,5))
	plt.title("Soil Moisture")
	plt.xlabel("time")
	plt.ylabel("%")
	plt.plot(date,dataSoil,'o-')
	plt.savefig('soilM.png')

	
	plt.figure(figsize=(20,5))
	plt.title("Temperature")
	plt.xlabel("time")
	plt.ylabel("°C")
	plt.plot(date,dataTemp,'o-')
	plt.savefig('temperature.png')
	storage.child("fig/humidity.png").put("humidity.png")
	storage.child("fig/soilM.png").put("soilM.png")
	storage.child("fig/temperature.png").put("temperature.png")
	print("draw fig complete")
def drawFigWithDate():	
	query = db.child("drawChart").get()
	value = query.val()
	if value != "none":
			
		params = value.split("@")
			
		def data(i):
			switcher={
				"Temperature":"tempDHT",
				"Humidity":"humDHT",
				"SoilM":'soilMoist',
				}
			return switcher.get(i,"*")
			
		sql2 = "select created_at,"+data(params[2])+" FROM `root_data` WHERE created_at BETWEEN DATE_ADD(STR_TO_DATE('"+params[0]+"','%d,%m,%Y'),INTERVAL -1 DAY) AND DATE_ADD(STR_TO_DATE('"+params[1]+"','%d,%m,%Y'),INTERVAL 1 DAY)"
		mycursor.execute(sql2)

		myresult2 = mycursor.fetchall()
		date = []
		dataGet = []
		for x in myresult2:
			date.append(x[0])
			dataGet.append(x[1])

		plt.figure(figsize=(15,5))
		plt.title(params[2])
		plt.plot(date,dataGet,'o-')
		plt.savefig('figByDate.png')
		storage.child("fig/figByDate.png").put("figByDate.png")
		db.child("drawChart").set("none")
		print("draw fig by date complete")

t1 = time.time() + 60
getData()
t2 = time.time() + 600
drawFig()

while True:
	
	drawFigWithDate()
	if time.time() >= t1:
		t1 = time.time() + 60
		getData()
		
	if time.time() >= t2:
		t2 = time.time() + 600
		drawFig()
		
