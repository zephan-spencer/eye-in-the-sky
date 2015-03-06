
public class HTML {

    public static String newArrayOpen() {
        return "var places = [";
    }
    public static String previousArrayOpen() {
        return "var places = ";
    }

    public static String arrayClose() {
        return "];";
    }

    public static String getHeader() {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "  <head>\n"
                + "    <style type=\"text/css\">\n"
                + "      html, body, #map-canvas { height: 100%; margin: 0; padding: 0;}\n"
                + "    </style>\n"
                + "    <script type=\"text/javascript\"\n"
                + "      src=\"https://maps.googleapis.com/maps/api/js?\">\n"
                + "    </script>\n"
                + "    <script type=\"text/javascript\">\n"
                + "function initialize() {\n"
                + "  var mapOptions = {\n"
                + "    zoom: 4,\n"
                + "    center: new google.maps.LatLng(40.866711, -101.465781)\n"
                + "  }\n"
                + "  var map = new google.maps.Map(document.getElementById('map-canvas'),\n"
                + "                                mapOptions);\n"
                + "\n"
                + "  setMarkers(map, places);\n"
                + "}";
    }

    public static String getFooter() {
        return "function setMarkers(map, locations) {\n"
                + "  // Add markers to the map\n"
                + "\n"
                + "  // Marker sizes are expressed as a Size of X,Y\n"
                + "  // where the origin of the image (0,0) is located\n"
                + "  // in the top left of the image.\n"
                + "\n"
                + "  // Origins, anchor positions and coordinates of the marker\n"
                + "  // increase in the X direction to the right and in\n"
                + "  // the Y direction down.\n"
                + "  // var image = {\n"
                + "  //   url: 'images/beachflag.png',\n"
                + "  //   // This marker is 20 pixels wide by 32 pixels tall.\n"
                + "  //   size: new google.maps.Size(20, 32),\n"
                + "  //   // The origin for this image is 0,0.\n"
                + "  //   origin: new google.maps.Point(0,0),\n"
                + "  //   // The anchor for this image is the base of the flagpole at 0,32.\n"
                + "  //   anchor: new google.maps.Point(0, 32)\n"
                + "  // };\n"
                + "  // Shapes define the clickable region of the icon.\n"
                + "  // The type defines an HTML &lt;area&gt; element 'poly' which\n"
                + "  // traces out a polygon as a series of X,Y points. The final\n"
                + "  // coordinate closes the poly by connecting to the first\n"
                + "  // coordinate.\n"
                + "  var shape = {\n"
                + "      coords: [1, 1, 1, 20, 18, 20, 18 , 1],\n"
                + "      type: 'poly'\n"
                + "  };\n"
                + "  for (var i = 0; i < locations.length; i++) {\n"
                + "    var place = locations[i];\n"
                + "    var myLatLng = new google.maps.LatLng(place[1], place[2]);\n"
                + "    var marker = new google.maps.Marker({\n"
                + "        position: myLatLng,\n"
                + "        map: map,\n"
                + "        // icon: image,\n"
                + "        shape: shape,\n"
                + "        title: place[0],\n"
                + "        zIndex: place[3]\n"
                + "    });\n"
                + "  }\n"
                + "}\n"
                + "\n"
                + "google.maps.event.addDomListener(window, 'load', initialize);\n"
                + "    </script>\n"
                + "\n"
                + "  </head>\n"
                + "  <body>\n"
                + "<div id=\"map-canvas\"></div>\n"
                + "  </body>\n"
                + "</html>";
    }
}
