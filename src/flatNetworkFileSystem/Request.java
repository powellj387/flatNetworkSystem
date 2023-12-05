//@author Julian Powell

package flatNetworkFileSystem;
import java.io.Serializable;

public class Request implements Serializable {
    private String method;
    private String fileName;
    private long fileData;

    public Request(String method, String fileName, long fileSize) {
        this.method = method;
        this.fileName = fileName;
        this.fileData = fileSize;
    }

    public String getMethod() {
        return method;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileData;
    }
}
