package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.net.URLEncoder;
import java.net.http.HttpResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class WeatherApp {
    public static void main(String[] args) throws Exception {

        try {
            Scanner sc = new Scanner(System.in);
            String x = null;
            String y = null;

            //	현재 위치를 입력받아서 getCoordination 메소드를 통해서 JSON 데이터를 가져온다.
            System.out.print("현재 위치를 입력하세요 : ");
            String address = sc.nextLine();
            String jsonStr = getCoordination(address);

            //	가져온 JSON 데이터를 파싱하여 위도 경도를 각각 y, x 변수로 저장.
            JSONObject json = ( JSONObject ) new JSONParser().parse( jsonStr );
            JSONArray jsonDocuments = (JSONArray) json.get( "documents" );
            if( jsonDocuments.size() != 0 ) {
                JSONObject j = (JSONObject) jsonDocuments.get(0);
                y = ( String ) j.get( "y" );
                x = ( String ) j.get( "x" );
            }

            //	입력받은 위치의 위도와 경도
            String lat = y; // 위도
            String lon = x; // 경도
            // System.out.println("위도: "+lat+"\t경도: "+lon);

            // 제외하고 싶은 데이터
            String[] part = { "current", "minutely", "hourly", "daily", "alerts" };

            // OpenAPI call하는 URL
            String urlstr = "https://api.openweathermap.org/data/2.5/onecall?" + "lat=" + lat + "&lon=" + lon
                    + "&lang=kr&exclude=" + part[1] + "," + part[4] + "&units=metric&appid=1eb1d18602c0e2dde562cdc2005a4495";
            URL url = new URL(urlstr);
            BufferedReader bf;
            String line;
            String result = "";

            // 날씨 정보를 받아온다.
            bf = new BufferedReader(new InputStreamReader(url.openStream()));

            // 버퍼에 있는 정보를 문자열로 변환.
            while ((line = bf.readLine()) != null) {
                result = result.concat(line);
                // 받아온 데이터를 확인.
//                 System.out.println(result);
            }

            // 문자열을 JSON으로 파싱
            // JSONParser
            JSONParser jsonParser = new JSONParser();
            // To JSONObject
            JSONObject jsonObj = (JSONObject) jsonParser.parse(result);

            System.out.println("1. 현재 날씨 조회\t2. 특정 시간 날씨 조회");
            int k = sc.nextInt();

            // 현재 날씨
            if (k == 1) {
                // 위치 출력
                System.out.println("위도 : " + jsonObj.get("lat") + "\t경도 : " + jsonObj.get("lon"));

                //  경기도 오산시 양산동 한신대길 127
//                // 날짜 및 시간 출력
                JSONObject currentArr = (JSONObject) jsonObj.get("current");
                String dt = currentArr.get("dt").toString();
                String dateStr = getTimestampToDate(dt);
                System.out.println("현재 시간: " + dateStr);

//                // 온도, 습도, 풍속 출력
                String temp = currentArr.get("temp").toString();
                System.out.println("현재 온도: " + temp);
                String humidity = currentArr.get("humidity").toString();
                System.out.println("현재 습도: " + humidity);
                String wind_speed = currentArr.get("wind_speed").toString();
                System.out.println("현재 풍속: " + wind_speed);


                // 날씨 출력
//                JSONObject cuWeatherArr = (JSONObject) jsonObj.get("weather");
//                String desc = cuWeatherArr.get("description").toString();
                JSONArray cuWeatherArr = (JSONArray) jsonObj.get("weather");
                JSONObject cuWeatherObj = (JSONObject) cuWeatherArr.get(0);

                Object timeObj_h = cuWeatherObj.get("description");
                String desc = String.valueOf(timeObj_h);
                System.out.println("날씨: " + desc);

            }
            // 특정 시간의 날씨
            else if (k == 2) {

                System.out.println("몇 시의 날씨를 조회하겠습니까?(금일로부터 48시간 이내: 1~48)");
                int i = sc.nextInt();

                JSONArray f_WeatherArray = (JSONArray) jsonObj.get("hourly");
                JSONObject f_WeatherObj = (JSONObject) f_WeatherArray.get(i);

                // 날짜 및 시간 출력
                Object timeObj_h = f_WeatherObj.get("dt");
                String str_h = String.valueOf(timeObj_h);
                String dateStr_h = getTimestampToDate(str_h);
                System.out.println("시간: " + dateStr_h);

                // 미래 날씨 출력
                JSONArray f_weatherArray_w = (JSONArray) f_WeatherObj.get("weather");
                JSONObject f_WeatherObj_w = (JSONObject) f_weatherArray_w.get(i);
                System.out.println("날씨 : " + f_WeatherObj_w.get("description"));

                // 미래 온도, 습도, 풍속 출력
                // JSONObject mainArray_hour = (JSONObject) jsonObj.get("hourly");
                double ktemp_h = Double.parseDouble(f_WeatherObj.get("temp").toString());
                double temp_h = ktemp_h - 273.15;
                int humidity_h = Integer.parseInt(f_WeatherObj.get("humidity").toString());
                int windSpeed_h = Integer.parseInt(f_WeatherObj.get("wind_speed").toString());
                System.out.printf("온도 : %.2f°C\n습도 : %d%\n풍속 : %.2fm/s", temp_h, humidity_h, windSpeed_h);
            }

            bf.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // unix timestamp to date String
    private static String getTimestampToDate(String timestampStr) {
        long timestamp = 0;
        try {
            timestamp = Long.parseLong(timestampStr);
            System.out.println(timestamp);
        } catch (Exception e) {
            System.out.println("The input string does not represent a valid number");
        }

        Date date = (Date) new java.util.Date(timestamp * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+9"));
        String formattedDate = sdf.format(date);

        return formattedDate;
    }

    //	kakao maps api JSON 데이터 가져오기
    private static String getCoordination( String address ) throws Exception {

        String encodeAddress = "";  // 한글 주소는 encoding 해서 날려야 함
        try {
            //	URLEncoder.encode 공백처리 문제로 400 오류 발생 가능해서 .replaceAll("\\+","%20") 을 붙여야? ㅇ... 일단 해보고 테스트
            encodeAddress = URLEncoder.encode( address, "UTF-8" );
        }
        catch ( UnsupportedEncodingException e ) {
            e.printStackTrace();
        }

        String apiUrl = "https://dapi.kakao.com/v2/local/search/address.json?query=" + encodeAddress;
        String auth = "KakaoAK 91ee832ce89440155c14ad98c1de0f74";

        URL url = new URL( apiUrl );
        HttpsURLConnection conn = ( HttpsURLConnection ) url.openConnection();
        conn.setRequestMethod( "GET" );
        conn.setRequestProperty( "Authorization", auth );

        BufferedReader br;

        int responseCode = conn.getResponseCode();
        if( responseCode == 200 ) {  // 호출 OK
            br = new BufferedReader( new InputStreamReader(conn.getInputStream(), "UTF-8") );
        } else {  // 에러
            br = new BufferedReader( new InputStreamReader(conn.getErrorStream(), "UTF-8") );
        }

        String jsonString = new String();
        String stringLine;
        while ( ( stringLine= br.readLine()) != null ) {
            jsonString += stringLine;
        }
        return jsonString;
    }
}