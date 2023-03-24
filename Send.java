import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class Send{
    
    public static void main(String[] args) throws Exception{ // nos pasan fichero, ip rx, port rx, ip emulador, port emulador

        if(args.length != 5){
            System.out.println("La forma correcta de invocacion es: Send input_file dest_IP dest_port emulador_IP emulador_Port");
            System.exit(-1);
        }

        FileInputStream fichero = new FileInputStream(args[0]);
        InetAddress direccionReceptor = InetAddress.getByName(args[1]);//direccion de la aplicacion receptora
        byte[] ipReceptor = direccionReceptor.getAddress();//creamos los 4 octetos de la direccion IP del receptor
        byte[] puertoReceptorAux = ByteBuffer.allocate(4).putInt(Integer.parseInt(args[2])).array(); //allocate 4 minimo
        byte[] puertoReceptor = new byte[2];//puerto aplicacion receptora
        puertoReceptor[0] = puertoReceptorAux[2]; puertoReceptor[1] = puertoReceptorAux[3];//creamos los bytes para el puerto del receptor
        InetAddress direccionEmulador = InetAddress.getByName(args[3]);// direccion shufflerouter 
        int puertoEmulador = Integer.parseInt(args[4]);//puerto shufflerouter
        int numeroSecuencia = 0;//numero secuencia de los paquetes
        int rto = 60; //retransmission timeout
        boolean rtoFinalizado = false;
        DatagramSocket socket = new DatagramSocket();
        byte[] mensaje = new byte[1472];//buffer para los paquetes
        byte[] bufferAck = new byte[6];//buffer para leer el ack recibido
        int datoFichero = 0;
        byte[] datosAux = new byte[1462];
        int contador;
    
        while(datoFichero != -1){
            contador = 0;

            for (int i = 0; i < 1462; i++){ //bucle para leer datos del fichero (max 1462)
                datoFichero = fichero.read();
                if(datoFichero == -1) break;
                datosAux[i] = (byte) datoFichero;
                contador++;
            }
            byte[] datos = new byte[contador];
            for (int j = 0; j < contador; j++){
                datos[j] = datosAux[j];
            }
            do{
                try{//bucle para el envio y posible renvio del mensaje

                    rtoFinalizado = false;
                    mensaje = ByteBuffer.allocate(10 + contador).put(ipReceptor).put(puertoReceptor).putInt(numeroSecuencia).put(datos).array();

                    DatagramPacket paquete = new DatagramPacket(mensaje, mensaje.length, direccionEmulador, puertoEmulador);

                    socket.send(paquete);
                    socket.setSoTimeout(rto);

                    DatagramPacket ack = new DatagramPacket(bufferAck, bufferAck.length);
                    socket.receive(ack);  //recibimos el ack
                    numeroSecuencia++;
                    
                }catch(SocketTimeoutException finRto){
                    rtoFinalizado = true;
                }
            }while(rtoFinalizado);
        }

        do{
            try{//bucle para el envio y posible renvio del mensaje tamanho 0 para marcar el fin de envio de paquetes

                rtoFinalizado = false;
                mensaje = ByteBuffer.allocate(6).put(ipReceptor).put(puertoReceptor).array();
                DatagramPacket paquete = new DatagramPacket(mensaje, mensaje.length, direccionEmulador, puertoEmulador);
                socket.send(paquete);
                socket.setSoTimeout(rto);

            }catch(SocketTimeoutException finRto){
                rtoFinalizado = true;
            }
        }while(rtoFinalizado);

        socket.close();
        fichero.close();
    }
}