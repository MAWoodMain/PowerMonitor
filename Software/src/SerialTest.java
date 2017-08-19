import com.pi4j.io.serial.*;

import java.io.IOException;
import java.util.Arrays;

/**
 * PowerMonitor
 * Created by Matthew Wood on 19/08/2017.
 */
public class SerialTest
{
    public SerialTest() throws IOException, InterruptedException
    {
        SerialConfig config = new SerialConfig();
        config.device(SerialPort.getDefaultPort())
            .baud(Baud._9600)
            .dataBits(DataBits._8)
            .stopBits(StopBits._1)
            .parity(Parity.NONE)
            .flowControl(FlowControl.NONE);

        Serial serial = SerialFactory.createInstance();

        serial.addListener(e -> {
            try
            {
                System.out.println("Received: " + Arrays.toString(e.getBytes()));
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
        });

        serial.open(config);
        Thread.sleep(10000);
    }

    public static void main(String[] args) throws IOException, InterruptedException
    {
        new SerialTest();
    }
}
