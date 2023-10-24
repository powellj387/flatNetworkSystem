package flatNetworkFileSystem;

import java.io.Serializable;

public class Response implements Serializable {
    private String message;
    private String error;
    private String value;

        public Response(String message, String error, String value) {
        this.message = message;
        this.error = error;
        this.value = value;
    }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getValue() {return value;}

        public void setError(String error) {this.error = error;}

        public void setValue(String value) {this.value = value;}

        public String getError() {return error;}
    }
