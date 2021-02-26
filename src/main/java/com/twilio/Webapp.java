package com.twilio;

import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VoiceGrant;
import com.twilio.twiml.voice.*;
import com.twilio.twiml.voice.Client;
import com.twilio.twiml.voice.Number;
import com.twilio.twiml.voice.Record;
import com.twilio.type.*;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.TwiMLException;
import com.twilio.http.TwilioRestClient;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import java.net.URI;

import static spark.Spark.afterAfter;
import static spark.Spark.get;
import static spark.Spark.post;

//import com.twilio.type.Client;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Character;


public class Webapp {

    static final String IDENTITY = "alice";
    static final String to = "alice";
    static final String CALLER_ID = "client:quick_start";
    // Use a valid Twilio number by adding to your account via https://www.twilio.com/console/phone-numbers/verified
    static final String CALLER_NUMBER = "3144899693";

    public static void main(String[] args) throws Exception {
        // Load the .env file into environment
        dotenv();

        // Log all requests and responses
        afterAfter(new LoggingFilter());

        get("/", (request, response) -> {
            return welcome();
        });

        post("/", (request, response) -> {
           return welcome();
        });

        /**
         * Creates an access token with VoiceGrant using your Twilio credentials.
         *
         * @returns The Access Token string
         */
        get("/accessToken", (request, response) -> {
            // Read the identity param provided
            final String identity = request.queryParams("identity") != null ? request.queryParams("identity") : IDENTITY;
            return getAccessToken(identity);
        });

        /**
         * Creates an access token with VoiceGrant using your Twilio credentials.
         *
         * @returns The Access Token string
         */
        post("/accessToken", (request, response) -> {
            // Read the identity param provided
            String identity = null;
            List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
            Map<String, String> params = toMap(pairs);
            try {
                identity = params.get("identity");
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
            return getAccessToken(identity != null ? identity : IDENTITY);
        });

        /**
         * Creates an endpoint that can be used in your TwiML App as the Voice Request Url.
         * <br><br>
         * In order to make an outgoing call using Twilio Voice SDK, you need to provide a
         * TwiML App SID in the Access Token. You can run your server, make it publicly
         * accessible and use `/makeCall` endpoint as the Voice Request Url in your TwiML App.
         * <br><br>
         *
         * @returns The TwiMl used to respond to an outgoing call
         */
//        get("/makeCall", (request, response) -> {
//            final String to = request.queryParams("to");
//            System.out.println(to);
//            return call(to);
//        });
        get("/makeCall", (request, response) -> {
            //final String to = request.queryParams("to");
            final String to = "alice";
            System.out.println(to);
            return mainMenu();
            //return call(to);
        });

        /**
         * Creates an endpoint that can be used in your TwiML App as the Voice Request Url.
         * <br><br>
         * In order to make an outgoing call using Twilio Voice SDK, you need to provide a
         * TwiML App SID in the Access Token. You can run your server, make it publicly
         * accessible and use `/makeCall` endpoint as the Voice Request Url in your TwiML App.
         *
         * <br><br>
         *
         * @returns The TwiMl used to respond to an outgoing call
         */
//        post("/makeCall", (request, response) -> {
//            String to = "";
//            List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
//            Map<String, String> params = toMap(pairs);
//            try {
//                to = params.get("to");
//            } catch (Exception e) {
//                return "Error: " + e.getMessage();
//            }
//            System.out.println(to);
//            return call(to);
//        });
        post("/makeCall", (request, response) -> {
//            String to = "";
//            List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
//            Map<String, String> params = toMap(pairs);
//            try {
//                to = params.get("to");
//            } catch (Exception e) {
//                return "Error: " + e.getMessage();
//            }
            String to = "alice";
            System.out.println(to);
            return mainMenu();
            //return call(to);
        });

        /**
         * Makes a call to the specified client using the Twilio REST API.
         *
         * @returns The CallSid
         */
        get("/placeCall", (request, response) -> {
            final String to = request.queryParams("to");
            // The fully qualified URL that should be consulted by Twilio when the call connects.
            URI uri = URI.create(request.scheme() + "://" + request.host() + "/incoming");
            System.out.println(uri.toURL().toString());
            return callUsingRestClient(to, uri);
        });

        /**
         * Makes a call to the specified client using the Twilio REST API.
         *
         * @returns The CallSid
         */
        post("/placeCall", (request, response) -> {
            String to = "";
            List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
            Map<String, String> params = toMap(pairs);
            try {
                to = params.get("to");
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
            // The fully qualified URL that should be consulted by Twilio when the call connects.
            URI uri = URI.create(request.scheme() + "://" + request.host() + "/incoming");
            System.out.println(uri.toURL().toString());
            return callUsingRestClient(to, uri);
        });

        /**
         * Creates an endpoint that plays back a greeting.
         */
        get("/incoming", (request, response) -> {
            return greet();
        });

        /**
         * Creates an endpoint that plays back a greeting.
         */
        post("/incoming", (request, response) -> {
            return greet();
        });
        //TODO:: handle unanswered call
        get("/handle-unanswered-call", (request, response) -> {
            String status = request.queryParams("DialCallStatus");
            System.out.println("didCallStatus: "+ status);
            if(status.equals("busy")||status.equals("no-answer")){
                return voicemail();
            }
            return new VoiceResponse.Builder().hangup(new Hangup.Builder().build()).build().toXml();
        });
        post("/handle-unanswered-call", (request, response) -> {
//            String status = "";
//            List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
//            Map<String, String> params = toMap(pairs);
//            try {
//                status = params.get("DialCallStatus");
//            } catch (Exception e) {
//                return "Error: " + e.getMessage();
//            }
            String status = request.queryParams("DialCallStatus");
            System.out.println("didCallStatus: "+ status);
            if(status.equals("busy")||status.equals("no-answer")){
                return voicemail();
            }
            return new VoiceResponse.Builder().hangup(new Hangup.Builder().build()).build().toXml();
        });
        //TODO::handle record voice mail
        get("/handle-record-voicemail",(request, response) -> {
            String recordingUrl = request.queryParams("RecordingUrl") +".mp3";
            String transcriptionUrl = request.queryParams("TranscriptionUrl");
            System.out.println("recordingUrl: "+ recordingUrl);
            System.out.println("transcriptionUrl: "+ recordingUrl);
            System.out.println("TEXT: "+request.queryParams("TranscriptionText"));
            Say say = new Say.Builder("Congratulations! You have leave your message").build();
            return new VoiceResponse.Builder().say(say).hangup(new Hangup.Builder().build()).build().toXml();

        });
        post("/handle-record-voicemail",(request, response) -> {
            String recordingUrl = "";
            String transciptionUrl = "" ;
            String text = "";
            List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
            Map<String, String> params = toMap(pairs);
            try {
                recordingUrl = params.get("RecordingUrl")+".mp3";
                transciptionUrl = params.get("TranscriptionUrl");
                text = params.get("TranscriptionText");
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
            System.out.println("recordingUrl: "+ recordingUrl);
            System.out.println("transcriptionUrl: "+ transciptionUrl);
            System.out.println("TEXT: "+text);
            Say say = new Say.Builder("Congratulations! You have leave your message").build();
            return new VoiceResponse.Builder().say(say).hangup(new Hangup.Builder().build()).build().toXml();
        });
        //TODO::option
        get("/option",(request, response)->{
            String selectedOption = request.queryParams("Digits");
            System.out.println("The digits is: " + selectedOption);
            String toXml;
            switch (selectedOption){
                case "1":
                    toXml = call(to) ;
                    break;
                case "2":
                    toXml= recordCall(to);
                    break;
                case "3":
                    toXml = forwardingCall(to);
                    break;
                case "4":
                    toXml = conference();
                    break;
                default:
                    toXml= call(to);
            }
            return toXml;
        });
        post("/option", (request, response) -> {
            String selectedOption = "";
            List<NameValuePair> pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
            Map<String, String> params = toMap(pairs);
            try {
                selectedOption = params.get("Digits");
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
            System.out.println("The digits is: " + selectedOption);
            String toXml;

            switch (selectedOption){
                case "1":
                    toXml = call(to) ;
                    break;
                case "2":
                    toXml= recordCall(to);
                    break;
                case "3":
                    toXml = forwardingCall(to);
                    break;
                case "4":
                    toXml = conference();
                    break;
                default:
                    toXml= call(to);
            }
            return toXml;
        });
        //TODO::handle recoding call
        get("/handle-recording-call", (request, response) -> {
            String recordingUrl = request.queryParams("RecordingUrl");
            System.out.println("recordingUrl: "+ recordingUrl);

            return recordingUrl;
        });
        post("/handle-recording-call", (request, response) -> {
            String recordingUrl = request.queryParams("RecordingUrl");
            System.out.println("recordingUrl: "+ recordingUrl);
            return recordingUrl;
        });
        //TODO::forward call
        get("/forward-call", (request, response) -> {
            VoiceResponse voiceResponse;
            Client client = new Client.Builder("bob").build();
            Dial dial = new Dial.Builder().callerId(CALLER_ID).client(client)
                    .timeout(10)
                    .build();
            voiceResponse = new VoiceResponse.Builder().dial(dial).build();
            return voiceResponse.toXml();
        });
        post("/forward-call", (request, response) -> {
            VoiceResponse voiceResponse;
            Client client = new Client.Builder("bob").build();
            Dial dial = new Dial.Builder().callerId(CALLER_ID).client(client)
                    .timeout(10)
                    .build();
            voiceResponse = new VoiceResponse.Builder().dial(dial).build();
            return voiceResponse.toXml();
        });
    }
    //TODO conference()
    private static String conference(){
        VoiceResponse response;
        Conference conference = new Conference.Builder("Room 1234").build();
        Dial dial = new Dial.Builder().conference(conference).build();
        Say say = new Say.Builder("Welcome to the room 1234").build();
        response = new VoiceResponse.Builder().dial(dial).build();
        return response.toXml();
    }
    //TODO:: forwardIngCall 1 step
    private static String forwardingCall(String to){
        VoiceResponse voiceResponse;
        Client client = new Client.Builder(to).build();
        Dial dial = new Dial.Builder().callerId(CALLER_ID).client(client)
                .timeout(10)
                .action("/forward-call")
                .build();
        voiceResponse = new VoiceResponse.Builder().dial(dial).build();
        return voiceResponse.toXml();
    }
    //TODO::make recording call
    private static String recordCall(String to){
        VoiceResponse voiceResponse;
        Client client = new Client.Builder(to).build();
        Dial dial = new Dial.Builder().callerId(CALLER_ID).client(client)
                .timeout(10)
                .action("/handle-unanswered-call")
                .method(HttpMethod.GET)
                .record(Dial.Record.RECORD_FROM_RINGING_DUAL)
                .recordingStatusCallback("/handle-recording-call")
                .build();
        voiceResponse = new VoiceResponse.Builder().dial(dial).build();
        return voiceResponse.toXml();
    }
    //TODO:: voicemail and make call implement
    private static String voicemail(){
        Say say = new Say
                .Builder("Please leave a message at the beep.\nPress the star key when finished.").build();
        Record record = new Record.Builder()
                .maxLength(20).finishOnKey("*")
                .transcribe(true)
                .transcribeCallback("/handle-record-voicemail")
                .build();
        Say say2 = new Say.Builder("I did not receive a recording").build();
        VoiceResponse response = new VoiceResponse.Builder().say(say)
                .record(record).say(say2).hangup(new Hangup.Builder().build()).build();
        return response.toXml();
    }
//    private static String sendVoicemail(){
//        Say say = new Say.Builder("Congratulations! You have sent the voice mail.").build();
//        return new VoiceResponse.Builder().say(say).build().toXml();
//    }
//    public static String makeCall(){
//        Say say = new Say.Builder("Congratulations! You have made a phone call.").build();
//        return new VoiceResponse.Builder().say(say).build().toXml();
//    }

    private static String getAccessToken(final String identity) {
        // Create Voice grant
        VoiceGrant grant = new VoiceGrant();
        grant.setOutgoingApplicationSid(System.getProperty("APP_SID"));
        grant.setPushCredentialSid(System.getProperty("PUSH_CREDENTIAL_SID"));

        // Create access token
        AccessToken token = new AccessToken.Builder(
                System.getProperty("ACCOUNT_SID"),
                System.getProperty("API_KEY"),
                System.getProperty("API_SECRET")
        ).identity(identity).grant(grant).build();
        System.out.println(token.toJwt());
        return token.toJwt();
    }


    //TODO:: main menu
    private static String mainMenu(){
        VoiceResponse voiceResponse;
        voiceResponse = new VoiceResponse.Builder()
                .gather(new Gather.Builder()
                        .numDigits(1)
                        .action("/option")
                        .say(new Say.Builder("For regular call, press 1. For recording call, press 2.").build())
                        .build()).build();
        System.out.println("main menu");
        return voiceResponse.toXml();
    }
    private static String call(final String to) {
        VoiceResponse voiceResponse;
        String toXml = null;
        if (to == null || to.isEmpty()) {
            Say say = new Say.Builder("Congratulations! You have made your first call! Good bye.").build();
            voiceResponse = new VoiceResponse.Builder().say(say).build();
        } else if (isPhoneNumber(to)) {
            Number number = new Number.Builder(to).build();
            Dial dial = new Dial.Builder().callerId(CALLER_NUMBER).number(number)
                    .timeout(10)
                    .action("/handle-unanswered-call")
                    .method(HttpMethod.GET)
                    .build();
            voiceResponse = new VoiceResponse.Builder().dial(dial).build();
        }else if(to.equals("music")){
            Play play = new Play.Builder("https://api.twilio.com/cowbell.mp3").loop(0).build();
            voiceResponse = new VoiceResponse.Builder().play(play).build();
        }
        else {
            Client client = new Client.Builder(to).build();
            Dial dial = new Dial.Builder().callerId(CALLER_ID).client(client)
                    .timeout(10)
                    .action("/handle-unanswered-call")
                    .method(HttpMethod.GET)
                    .build();
            voiceResponse = new VoiceResponse.Builder().dial(dial).build();
        }
        try {
            toXml = voiceResponse.toXml();
        } catch (TwiMLException e) {
            e.printStackTrace();
        }
        return toXml;
    }

    private static String callUsingRestClient(final String to, final URI uri) {
        final TwilioRestClient client = new TwilioRestClient.Builder(System.getProperty("API_KEY"), System.getProperty("API_SECRET"))
                .accountSid(System.getProperty("ACCOUNT_SID"))
                .build();

        if (to == null || to.isEmpty()) {
            com.twilio.type.Client clientEndpoint = new com.twilio.type.Client("client:" + IDENTITY);
            PhoneNumber from = new PhoneNumber(CALLER_ID);
            // Make the call
            Call call = Call.creator(clientEndpoint, from, uri).setMethod(HttpMethod.GET).create(client);
            // Print the call SID (a 32 digit hex like CA123..)
            System.out.println(call.getSid());
            return call.getSid();
        } else if (isNumeric(to)) {
            com.twilio.type.Client clientEndpoint = new com.twilio.type.Client(to);
            PhoneNumber from = new PhoneNumber(CALLER_NUMBER);
            // Make the call
            Call call = Call.creator(clientEndpoint, from, uri).setMethod(HttpMethod.GET).create(client);
            // Print the call SID (a 32 digit hex like CA123..)
            System.out.println(call.getSid());
            return call.getSid();
        } else {
            com.twilio.type.Client clientEndpoint = new com.twilio.type.Client("client:" + to);
            PhoneNumber from = new PhoneNumber(CALLER_ID);
            // Make the call
            Call call = Call.creator(clientEndpoint, from, uri).setMethod(HttpMethod.GET).create(client);
            // Print the call SID (a 32 digit hex like CA123..)
            System.out.println(call.getSid());
            return call.getSid();
        }
    }

    private static String greet() {
        VoiceResponse voiceResponse;
        Say say = new Say.Builder("Congratulations! You have received your first inbound call! Good bye.").build();
        voiceResponse = new VoiceResponse.Builder().say(say).build();
        System.out.println(voiceResponse.toXml().toString());
        return voiceResponse.toXml();
    }

    private static String welcome() {
        VoiceResponse voiceResponse;
        Say say = new Say.Builder("Welcome to Twilio").build();
        voiceResponse = new VoiceResponse.Builder().say(say).build();
        System.out.println(voiceResponse.toXml().toString());
        return voiceResponse.toXml();
    }

    private static void dotenv() throws Exception {
        final File env = new File(".env");
        if (!env.exists()) {
            return;
        }

        final Properties props = new Properties();
        props.load(new FileInputStream(env));
        props.putAll(System.getenv());
        props.entrySet().forEach(p -> System.setProperty(p.getKey().toString(), p.getValue().toString()));
    }

    private static Map<String, String> toMap(final List<NameValuePair> pairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.size(); i++) {
            NameValuePair pair = pairs.get(i);
            System.out.println("NameValuePair - name=" + pair.getName() + " value=" + pair.getValue());
            map.put(pair.getName(), pair.getValue());
        }
        return map;
    }

    private static boolean isPhoneNumber(String s) {
        if (s.length() == 1) {
            return isNumeric(s);
        } else if (s.charAt(0) == '+') {
            return isNumeric(s.substring(1));
        } else {
            return isNumeric(s);
        }
    }

    private static boolean isNumeric(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
