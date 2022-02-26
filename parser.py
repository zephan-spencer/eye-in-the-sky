# importing geopy library
from geopy.geocoders import Nominatim
import pandas as pd
import time
import folium

df = pd.read_csv('contacts.csv')

desiredColumns = ['First Name', 'Last Name', "Email", "Phone", "Mailing Street", "Mailing City", "Mailing State", "Mailing Zip", "ID/Status"]

test = df[desiredColumns].dropna()

# calling the Nominatim tool
loc = Nominatim(user_agent="GetLoc")

m = folium.Map(location=[40, -95], zoom_start=4)

for index, row in test.iterrows():
    currentAddress = {
      "street": row["Mailing Street"],
      "city": row["Mailing City"],
      "state": row["Mailing State"],
      "postalcode": row["Mailing Zip"]
    }
    status = row["ID/Status"]
    # Check if status is invalid
    if status == "Prosthetist" or status == "Nurse Case Manager" or status == "Doctor" or status == "Therapist":
        print("Ignoring, invalid status")
        continue
    # Geocode!
    getLoc = loc.geocode(currentAddress)
    # Wait for API limitation 
    time.sleep(2)
    # Make sure we got something back
    if getLoc is None:
        print("Got nothing")
    else:
        print(getLoc.address)
        print("Latitude = ", getLoc.latitude, "\n")
        print("Longitude = ", getLoc.longitude)
        if status == "Workers Comp":
            folium.Marker(
                location=[getLoc.latitude, getLoc.longitude],
                popup=(row["First Name"] + " " + row["Last Name"] + " " + row["Email"] + " " + row["Phone"] + " " + status),
                icon=folium.Icon(color="red", icon="star", prefix='fa'),
            ).add_to(m)
        elif status.lower() == "INACTIVE":
            folium.Marker(
                location=[getLoc.latitude, getLoc.longitude],
                popup=(row["First Name"] + " " + row["Last Name"] + " " + row["Email"] + " " + row["Phone"] + " " + status),
                icon=folium.Icon(color="blue", icon="archive", prefix='fa'),
            ).add_to(m)
        elif "Client" in status:
            folium.Marker(
                location=[getLoc.latitude, getLoc.longitude],
                popup=(row["First Name"] + " " + row["Last Name"] + " " + row["Email"] + " " + row["Phone"] + " " + status),
                icon=folium.Icon(color="green", icon="dollar", prefix='fa'),
            ).add_to(m)            
        else:
            folium.Marker(
                location=[getLoc.latitude, getLoc.longitude],
                popup=(row["First Name"] + " " + row["Last Name"] + " " + row["Email"] + " " + row["Phone"] + " " + status),
                icon=folium.Icon(color="orange", icon="comment", prefix='fa'),
            ).add_to(m)

m.save("test.html")