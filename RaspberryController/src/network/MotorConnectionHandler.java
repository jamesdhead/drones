package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import network.messages.Message;
import network.messages.MotorMessage;
import network.messages.SystemStatusMessage;
import main.Controller;

public class MotorConnectionHandler extends ConnectionHandler {

	public MotorConnectionHandler(Socket socket, Controller controller,
			ConnectionListener connectionListener) {
		super(socket, controller, connectionListener);
	}
	
	@Override
	protected void shutdownHandler() {
		controller.processMotorMessage(new MotorMessage(0, 0));
		closeConnection();
	}
	
	@Override
	protected void processMessage(Message message) {
		if (message instanceof MotorMessage) {
			controller.processMotorMessage(((MotorMessage) message));
		}
	}
	
	protected void initConnection() throws IOException, ClassNotFoundException {
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());

		out.reset();

		out.writeObject(InetAddress.getLocalHost().getHostName());
		out.flush();

		clientName = (String) in.readObject();

		System.out.println("[CONNECTION HANDLER] Client "
				+ socket.getInetAddress().getHostAddress() + " (" + clientName
				+ ") connected");

		// controller.processInformationRequest(new
		// InformationRequest(MessageType.SYSTEM_STATUS), this);
		/*sendData(new SystemStatusMessage(controller.getInitialMessages()));*/
	}
}