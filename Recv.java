import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class Recv{
    
    public static void main(String[] args) throws Exception{ // nos pasan fichero, ip rx, port rx, ip emulador, port emulador
    
        if(args.length != 2){
            System.out.println("La forma correcta de invocacion es: Recv output_file listen_port");
            System.exit(-1);
        }
        FileOutputStream fichero = new FileOutputStream(args[0]); //fichero para escribir los datos
        DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[1]));
        byte[] paqueteRecivido = new byte[1472];
        byte[] numeroSecuenciaBytes = new byte[4];
        int numeroSecuencia;
        int numeroSecuenciaAnterior = -1;
        byte[] datos;
        try{
            DatagramPacket peticion = new DatagramPacket(paqueteRecivido, paqueteRecivido.length);
            socket.receive(peticion);
            do{
                paqueteRecivido = peticion.getData();

                for(int k = 0; k < 4; k++){//bucle para obtener el numero de secuencia
                    numeroSecuenciaBytes[k] = paqueteRecivido[6+k];
                }
                numeroSecuencia = ByteBuffer.wrap(numeroSecuenciaBytes).getInt();//pasamos de bytes a int
                datos = new byte[peticion.getLength()-10];

                for(int y = 0; y < peticion.getLength()-10; y++){//bucle para la obtencion de datos
                    datos[y] = paqueteRecivido[10 + y];
                }
                if(numeroSecuencia != numeroSecuenciaAnterior){
                    fichero.write(datos);
                    numeroSecuenciaAnterior = numeroSecuencia;
                    numeroSecuencia++;
                }
                byte[] datosTx = new byte[6];
                for(int g = 0; g < 6; g++){// bucle para obtener la ip y el puerto del transmisor
                    datosTx[g] = paqueteRecivido[g];
                }
                byte[] respuesta = new byte[6];
                respuesta = ByteBuffer.allocate(6).put(datosTx).array();
                InetAddress direccionEmulador = peticion.getAddress();
                DatagramPacket paqueteRespuesta = new DatagramPacket(respuesta, respuesta.length, direccionEmulador, peticion.getPort());
                socket.send(paqueteRespuesta);//enviamos la respuesta

                peticion = new DatagramPacket(paqueteRecivido, paqueteRecivido.length);
                socket.receive(peticion);//recibiomos la peticion
            }while(peticion.getLength() != 6);//se comprueba que no es el paquete de fin de transmision

        } catch (Exception ex) {
            System.exit(-1);
        }
        fichero.close();
        socket.close();
    }
}