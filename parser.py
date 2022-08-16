# importing geopy library
from geopy.geocoders import Nominatim
import pandas as pd
import time
import folium

m = folium.Map(location=[40, -95], zoom_start=4)

def main():
    contact_file = pd.read_csv('contacts.csv')
    lead_file = pd.read_csv('leads.csv')

    needed_contact_columns = ['First Name', 'Last Name', "Email", "Phone", "Mailing Street", "Mailing City", "Mailing State", "Mailing Zip", "ID/Status"]

    needed_lead_columns = ['First Name', 'Last Name', "Email", "Phone", "Street", "City", "State", "Zip Code", "ID/Status"]

    contacts = contact_file[needed_contact_columns].dropna()
    leads = lead_file[needed_lead_columns].dropna()

    # calling the Nominatim tool
    loc = Nominatim(user_agent="GetLoc")


    for index, row in contacts.iterrows():
        
        current_contact_address = {
          "street": row["Mailing Street"],
          "city": row["Mailing City"],
          "state": row["Mailing State"],
          "postalcode": row["Mailing Zip"]
        }

        status = row["ID/Status"]
        color, icon, icon_color = get_type(status)

        if color == None:
            continue

        locate(loc, row, status, color, icon, icon_color, current_contact_address)

    for index, row in leads.iterrows():
        
        current_lead_address = {
          "street": row["Street"],
          "city": row["City"],
          "state": row["State"],
          "postalcode": row["Zip Code"]
        }

        status = row["ID/Status"]
        color, icon, icon_color = get_type(status)

        if color == None:
            continue

        locate(loc, row, status, color, icon, icon_color, current_lead_address)

    m.save("test.html")

def locate(loc, row, status, color, icon, icon_color, current_address):
        # Get location
        get_loc = loc.geocode(current_address)

        # Wait for API limitation 
        time.sleep(2)
        # Make sure we got something back
        if get_loc is None:
            print("Got nothing")
        else:
            # Get marker params
            address = get_loc.address
            latitude = get_loc.latitude
            longitude = get_loc.longitude
            first_name = row["First Name"]
            last_name = row["Last Name"]
            email = row["Email"]
            phone = row["Phone"]
            # Printout the address and add it to the map
            print("Address: " + address + "\t" + "Status: " + status)
            add_to_map(status, latitude, longitude, first_name, last_name, email, phone, color, icon, icon_color)


def get_type(status):
    
    icon_color = "white"

    if status == "Workers Comp":
        color = "red"
        icon = "star"
    elif status == "WC Client":
        color = "orange"
        icon = "dollar"
    elif "Client" in status:
        color = "green"
        icon = "dollar"
    elif status == "Nurse Case Manager" or status == "Doctor" or status == "Therapist":
        color = "purple"
        icon = "medkit"
    elif status == "Amputee - good progress" or status == "Potential Client":
        print("Found it!")
        color = "darkpurple"
        icon = "thumbs-up"
    elif status == "Amputee":
        color = "lightblue"
        icon = "question"
    else:
        color = None
        icon = None

    return color, icon, icon_color

def add_to_map(status, latitude, longitude, first_name, last_name, email, phone, color, icon, icon_color):
    
    popup_string = first_name + " " + last_name + " " + email + " " + phone + " " + status 
    
    create_marker(color, icon, icon_color, latitude, longitude, popup_string)

def create_marker(color, icon, icon_color, latitude, longitude, popup_string):
    folium.Marker(
        location=[latitude, longitude],
        popup=(popup_string),
        icon=folium.Icon(color=color, icon=icon, icon_color=icon_color, prefix='fa'),
    ).add_to(m)

if __name__ == "__main__":
    main()