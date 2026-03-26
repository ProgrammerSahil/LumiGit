import urllib.request
import json
def get_aqi(city):
	city = city.replace(" ", "-")

	url = "https://api.waqi.info/feed/"+city+"/?token=04b22c90babd2d3fcfe3db3c83c4e18b9c4abdc8"
	try:
		JSONdata = urllib.request.urlopen(url).read()
		aqi = int(json.loads(JSONdata)["data"]["aqi"])
		return aqi
	except:
		return "City not found!"

while 1:
	city = str(input("Enter the required city : "))
	print(get_aqi(city))
