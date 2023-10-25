//@author Julian Powell

package flatNetworkFileSystem;
import java.io.Serializable;

public class Request implements Serializable {
    private String method;
    private String fileName;
    private byte[] fileData;

    public Request(String method, String fileName, byte[] fileData) {
        this.method = method;
        this.fileName = fileName;
        this.fileData = fileData;
    }

    public String getMethod() {
        return method;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }
}
