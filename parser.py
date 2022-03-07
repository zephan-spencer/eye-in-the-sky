# importing geopy library
from geopy.geocoders import Nominatim
import pandas as pd
import time
import folium

m = folium.Map(location=[40, -95], zoom_start=4)

def main():
    contactCSV = pd.read_csv('contacts.csv')

    desiredColumns = ['First Name', 'Last Name', "Email", "Phone", "Mailing Street", "Mailing City", "Mailing State", "Mailing Zip", "ID/Status"]

    test = contactCSV[desiredColumns].dropna()

    # calling the Nominatim tool
    loc = Nominatim(user_agent="GetLoc")

    for index, row in test.iterrows():
        currentAddress = {
          "street": row["Mailing Street"],
          "city": row["Mailing City"],
          "state": row["Mailing State"],
          "postalcode": row["Mailing Zip"]
        }
        status = row["ID/Status"]
        # Check if status is invalid
            # print("Ignoring, invalid status")
            # continue
        # Geocode!
        getLoc = loc.geocode(currentAddress)
        # Wait for API limitation 
        time.sleep(1.5)
        # Make sure we got something back
        if getLoc is None:
            print("Got nothing")
        else:
            # Get marker params
            address = getLoc.address
            latitude = getLoc.latitude
            longitude = getLoc.longitude
            firstName = row["First Name"]
            lastName = row["Last Name"]
            email = row["Email"]
            phone = row["Phone"]
            # Printout the address and add it to the map
            print(address)
            addToMap(status, latitude, longitude, firstName, lastName, email, phone)

    m.save("test.html")

def addToMap(status, latitude, longitude, firstName, lastName, email, phone):
    
    popupString = firstName + " " + lastName + " " + email + " " + phone+ " " + status

    if status == "Workers Comp":
        color = "red"
        icon = "star"
    elif status == "INACTIVE" or status == "Distributor":
        color = "gray"
        icon = "archive"
    elif status == "WC Client":
        color = "orange"
        icon = "dollar"
    elif "Client" in status:
        color = "green"
        icon = "dollar"
    elif "Nurse Case Manager" or "Therapist" or "Doctor" or "Therapist" or "Prosthetist" in status:
        color = "purple"
        icon = "medkit"
    else:
        color = "red"
        icon = "comment"
    
    createMarker(color, icon, latitude, longitude, popupString)

def createMarker(color, icon, latitude, longitude, popupString):
    folium.Marker(
        location=[latitude, longitude],
        popup=(popupString),
        icon=folium.Icon(color=color, icon=icon, prefix='fa'),
    ).add_to(m)

if __name__ == "__main__":
    main()