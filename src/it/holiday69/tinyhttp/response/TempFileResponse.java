package it.holiday69.tinyhttp.response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class TempFileResponse extends HttpResponse
{
  private File _file;
  private FileOutputStream _out;

  public TempFileResponse(File file)
    throws IOException
  {
    _out = new FileOutputStream(file);
    _file = file;
  }

  public TempFileResponse(String prefix, String postfix) throws IOException {
    this(File.createTempFile(prefix, postfix));
  }

  public TempFileResponse() throws IOException {
    this(File.createTempFile(UUID.randomUUID().toString().substring(0, 6), null));
  }

  public OutputStream getOutputStream() {
    return _out;
  }
  public File getFile() { return _file; }

}