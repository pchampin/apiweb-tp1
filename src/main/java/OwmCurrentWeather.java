import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


/**
 * OwmCurrentWeather client for current weather.
 *
 * Created by pchampin on 21/08/15.
 */
public class OwmCurrentWeather {

    /**
     * Create weather report for the given city.
     * @param city city name
     * @throws IOException
     */
    public static OwmCurrentWeather ofCity(String city) throws IOException {
        return new OwmCurrentWeather("q=" + city);
    }

    /**
     * Create weather report for the given city in a given country.
     * @param city city name
     * @param countryCode ISO 3166 country code
     * @throws IOException
     */
    public static OwmCurrentWeather ofCity(String city, String countryCode) throws IOException {
        throw new NotImplementedException();
    }

    /**
     * Create weather report for the city identified by cityId.
     * @param cityId OpenWeatherMap ID of  the city
     * @throws IOException
     */
    public static OwmCurrentWeather ofCityId(String cityId) throws IOException {
        throw new NotImplementedException();
    }

    /**
     * Create weather report for the given geographic coordinates
     * @param lon longitude
     * @param lat latitude
     * @throws IOException
     */
    public static OwmCurrentWeather ofGeo(double lat, double lon) throws IOException {
        return new OwmCurrentWeather("lon=" + lon + "&lat=" + lat);
    }

    /**
     * Create weather report for the given zipcode
     * @param zipCode zip code
     * @param countryCode ISO 3166 country code
     * @throws IOException
     */
    public static OwmCurrentWeather ofGeo(String zipCode, String countryCode) throws IOException {
        throw new NotImplementedException();
    }




    private String selector;
    private CloseableHttpClient httpClient;

    public OwmCurrentWeather(String selector) throws IOException {
        this.selector = selector;
        httpClient = HttpClients.createDefault();
        refresh();
    }


    private Document dom;
    private String cityId, cityName, countryCode, windName, windDirectionCode, windDirectionName, cloudinessName, precipitationMode, weatherValue;
    private double longitude, latitude, temp, tempMin, tempMax, humidity, pressure, windSpeed, windDirection, cloudiness, visibility, precipitation;
    private Instant sunrise, sunset, lastUpdate;

    /**
     * Refresh data of this weather report.
     * @throws IOException
     */
    public void refresh() throws IOException {

        String url = "http://api.openweathermap.org/data/2.5/weather?" + selector
                + "&units=metric&lang=fr&mode=xml";
        System.err.println(url);
        HttpGet hget = new HttpGet(url);
        CloseableHttpResponse resp = httpClient.execute(hget);
        DOMParser p = new DOMParser();
        try {
            p.parse(new InputSource(resp.getEntity().getContent()));
        }
        catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
        dom = p.getDocument();

        cityId = getStringFromDOM("city", "id");
        cityName = getStringFromDOM("city", "name");
        longitude = getDoubleFromDOM("coord", "lon");
        latitude = getDoubleFromDOM("coord", "lat");
        countryCode = getStringFromDOM("country");
        sunrise = getInstantFromDOM("sun", "rise");
        sunset= getInstantFromDOM("sun", "set");

        temp = getDoubleFromDOM("temperature", "value");
        tempMin = getDoubleFromDOM("temperature", "min");
        tempMax = getDoubleFromDOM("temperature", "max");
        humidity = getDoubleFromDOM("humidity", "value");
        pressure = getDoubleFromDOM("pressure", "value");

        windSpeed = getDoubleFromDOM("speed", "value");
        windName = getStringFromDOM("speed", "name");
        windDirection = getDoubleFromDOM("direction", "value");
        windDirectionCode = getStringFromDOM("direction", "code");
        windDirectionName = getStringFromDOM("direction", "name");

        cloudiness = getDoubleFromDOM("cloudiness", "value");
        cloudinessName = getStringFromDOM("cloudiness", "name");
        visibility = getDoubleFromDOM("visibility", "value");

        precipitation = getDoubleFromDOM("precipitation", "value");
        precipitationMode = getStringFromDOM("precipitation", "mode");

        weatherValue = getStringFromDOM("weather", "value");

        lastUpdate = getInstantFromDOM("lastupdate", "value");
    }


    private final String getStringFromDOM(String element) {
        NodeList nodes =  dom.getElementsByTagName(element);
        if (nodes.getLength() == 0) return null;
        return nodes.item(0).getTextContent();
    }

    private final String getStringFromDOM(String element, String attribute) {
        NodeList nodes =  dom.getElementsByTagName(element);
        if (nodes.getLength() == 0) return null;
        Node attNode = nodes
                .item(0)
                .getAttributes()
                .getNamedItem(attribute);
        if (attNode == null) return null;
        return attNode.getTextContent();
    }

    private final Instant getInstantFromDOM(String element, String attribute) {
        String attValue = getStringFromDOM(element, attribute);
        if (attValue == null) return null;
        if (attValue.length() == 19) attValue += "Z"; // no explicit timezone, so assume it is UTC
        return ZonedDateTime.parse(attValue).toInstant();
    }

    private final double getDoubleFromDOM(String element, String attribute) {
        String attValue = getStringFromDOM(element, attribute);
        if (attValue == null) return Double.NaN;
        return Double.parseDouble(attValue);
    }

    //////////////// Getters ////////////////

    /**
     * The OWM ID of the city concerned by this weather report.
     */
    public String getCityId() {
        return cityId;
    }

    /**
     * The name of the city concerned by this weather report.
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * The country code of the city concerned by this weather report (e.g. "GB").
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * A description of the wind condition.
     */
    public String getWindName() {
        return windName;
    }

    /**
     * The direction of the wind as a code (e.g. "SSW").
     */
    public String getWindDirectionCode() {
        return windDirectionCode;
    }

    /**
     * The direction of the wind in natural language (e.g. "South-southwest").
     */
    public String getWindDirectionName() {
        return windDirectionName;
    }

    /**
     * The cloudiness condition in natural language (e.g. "scattered clouds").
     */
    public String getCloudinessName() {
        return cloudinessName;
    }

    /**
     * The precipitation mode (e.g. "no", "rain", "snow").
     */
    public String getPrecipitationMode() {
        return precipitationMode;
    }

    /**
     * The general description of the weather.
     */
    public String getWeatherValue() {
        return weatherValue;
    }

    /**
     * The longitude of the city concerned with this weather report.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * The latitude of the city concerned with this weather report.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * The temperature (in Celsius).
     */
    public double getTemp() {
        return temp;
    }

    /**
     * The minimum temperature (in Celsius).
     * This is deviation from 'getTemp' that is possible for large cities and
     * megalopolises geographically expanded (use these parameter optionally).
     */
    public double getTempMin() {
        return tempMin;
    }

    /**
     * The maximum temperature (in Celsius).
     * This is deviation from 'getTemp' that is possible for large cities and
     * megalopolises geographically expanded (use these parameter optionally).
     */
    public double getTempMax() {
        return tempMax;
    }

    /**
     * The humidity percentage (between 0 and 100).
     */
    public double getHumidity() {
        return humidity;
    }

    /**
     * The pressure in hPa.
     */
    public double getPressure() {
        return pressure;
    }

    /**
     * The wind speed in m/s.
     */
    public double getWindSpeed() {
        return windSpeed;
    }

    /**
     * The wind direction in degrees (meteorological).
     */
    public double getWindDirection() {
        return windDirection;
    }

    /**
     * The cloud coverage in % (between 0 and 100).
     */
    public double getCloudiness() {
        return cloudiness;
    }

    /**
     * The visibility in m.
     */
    public double getVisibility() {
        return visibility;
    }

    /**
     * The precipitation level in mm.
     */
    public double getPrecipitation() {
        return precipitation;
    }

    /**
     * Return the time of sunrise.
     */
    public Instant getSunrise() {
        return sunrise;
    }

    /**
     * Return the time of sunset.
     */
    public Instant getSunset() {
        return sunset;
    }


    /**
     * The time when the data of this weather report was collected.
     *
     * NB: this is the time when the OWM servers build the weather report,
     * *not* the time when the refresh() method was called.
     */
    public Instant getLastUpdate() {
        return lastUpdate;
    }



    /**
     * Test function.
     */
    public static void main(String args[]) throws Exception {
        OwmCurrentWeather report = OwmCurrentWeather.ofCity("Lyon");
        //OwmCurrentWeather report = OwmCurrentWeather.ofGeo(45.7589, 4.8156);

        System.out.println("City ID:      " + report.getCityId());
        System.out.println("City name:    " + report.getCityName());
        System.out.println("Latitude:     " + report.getLatitude() + " °");
        System.out.println("Longitude:    " + report.getLongitude() + " °");
        System.out.println("Country code: " + report.getCountryCode());
        System.out.println("Sunrise:      " + report.getSunrise().atZone(ZoneId.systemDefault()));
        System.out.println("Sunset:       " + report.getSunset().atZone(ZoneId.systemDefault()));

        System.out.println("Temperature:     " + report.getTemp() + " °C");
        System.out.println("Temperature min: " + report.getTempMin() + " °C");
        System.out.println("Temperature max: " + report.getTempMax() + " °C");
        System.out.println("Humidity:        " + report.getHumidity() + " %");
        System.out.println("Pressure:        " + report.getPressure() + " hPa");

        System.out.println("Wind speed:     " + report.getWindSpeed() + " m/s");
        System.out.println("Wind name:      " + report.getWindName());
        System.out.println("Wind direction: " + report.getWindDirectionName()
                + "(" + report.getWindDirectionCode() + ")");
        System.out.println("Wind direction: " + report.getWindDirection() + " °");

        System.out.println("Cloud coverage: " + report.getCloudiness() + " %");
        System.out.println("Cloudiness:     " + report.getCloudinessName());
        System.out.println("Visibility:     " + report.getVisibility() + " m");

        System.out.println("Precipitation: " + report.getPrecipitation() + " mm of " + report.getPrecipitationMode());

        System.out.println("Weather:     " + report.getWeatherValue());
        System.out.println("Last update: " + report.getLastUpdate().atZone(ZoneId.systemDefault()));
    }
}
