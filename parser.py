# importing geopy library
from geopy.geocoders import Nominatim
import pandas as pd

df = pd.read_csv('contacts.csv')

desiredColumns = ['First Name', 'Last Name', "Email", "Phone", "Mailing Street", "Mailing City", "Mailing State", "Mailing Zip", "ID/Status"]

test = df[desiredColumns].dropna()

# calling the Nominatim tool
loc = Nominatim(user_agent="GetLoc")

for index, row in test.iterrows():
    # entering the location name
    address = row["Mailing Street"] + " " + row["Mailing City"] + " " + row["Mailing State"] + ", "+ row["Mailing Zip"]
    # print(address)
    getLoc = loc.geocode(address)
    print(getLoc.address)
    print("Latitude = ", getLoc.latitude, "\n")
    print("Longitude = ", getLoc.longitude)  
    if index > 2:
        break