package ch.ethz.systems.netbench.xpt.newreno;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.log.SimulationLogger;
import ch.ethz.systems.netbench.xpt.newreno.TcpRetransmissionTimeOutEvent;
import ch.ethz.systems.netbench.xpt.newreno.newrenotcp.NewRenoTcpSocket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TcpRetransmissionTimeOutEventTest {

    @Before
    public void setup() {

        Simulator.setup(0);
    }

    @After
    public void cleanup() {

        Simulator.reset();
    }

    @Test
    public void testTriggerNormal() {
        NewRenoTcpSocket socket = mock(NewRenoTcpSocket.class);
        TcpRetransmissionTimeOutEvent event = new TcpRetransmissionTimeOutEvent(1000, socket);
        event.trigger();
        verify(socket, times(1)).handleRetransmissionTimeOut();
    }

    @Test
    public void testTriggerCanceled() {
        NewRenoTcpSocket socket = mock(NewRenoTcpSocket.class);
        TcpRetransmissionTimeOutEvent event = new TcpRetransmissionTimeOutEvent(1000, socket);
        event.cancel();
        event.trigger();
        verify(socket, times(0)).handleRetransmissionTimeOut();
    }

    @Test
    public void testTriggerInSimulation() {
        NewRenoTcpSocket socket = mock(NewRenoTcpSocket.class);
        TcpRetransmissionTimeOutEvent event = new TcpRetransmissionTimeOutEvent(1000, socket);
        Simulator.registerEvent(event);
        Simulator.runNs(2000);
        verify(socket, times(1)).handleRetransmissionTimeOut();
    }

    @Test
    public void testTriggerInSimulationJustNot() {
        NewRenoTcpSocket socket = mock(NewRenoTcpSocket.class);
        TcpRetransmissionTimeOutEvent event = new TcpRetransmissionTimeOutEvent(1000, socket);
        Simulator.registerEvent(event);
        Simulator.runNs(999);
        verify(socket, times(0)).handleRetransmissionTimeOut();
    }

    @Test
    public void testTriggerInSimulationJust() {
        NewRenoTcpSocket socket = mock(NewRenoTcpSocket.class);
        TcpRetransmissionTimeOutEvent event = new TcpRetransmissionTimeOutEvent(999, socket);
        Simulator.registerEvent(event);
        Simulator.runNs(999);
        verify(socket, times(1)).handleRetransmissionTimeOut();
    }

    @Test
    public void testTriggerInSimulationCanceled() {
        NewRenoTcpSocket socket = mock(NewRenoTcpSocket.class);
        TcpRetransmissionTimeOutEvent event = new TcpRetransmissionTimeOutEvent(999, socket);
        event.cancel();
        Simulator.registerEvent(event);
        Simulator.runNs(999);
        verify(socket, times(0)).handleRetransmissionTimeOut();
    }

    @Test
    public void testToString() {
        NewRenoTcpSocket socket = mock(NewRenoTcpSocket.class);
        TcpRetransmissionTimeOutEvent event = new TcpRetransmissionTimeOutEvent(999, socket);
        event.toString();
    }

}
