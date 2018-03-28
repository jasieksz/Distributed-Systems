import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.View;

import java.util.Collections;
import java.util.List;

public class ViewHandler extends Thread {
    private final JChannel jChannel;
    private final MergeView mergeView;

    public ViewHandler(JChannel jChannel, MergeView mergeView) {
        this.jChannel = jChannel;
        this.mergeView = mergeView;
    }

    @Override
    public void run() {
        List<View> subgroups = Collections.synchronizedList(mergeView.getSubgroups());
        View tmp_view = subgroups.get(0);
        Address local_addr = jChannel.getAddress();
        if (!tmp_view.getMembers().contains(local_addr)) {
            System.out.println("Not member of the new primary partition (" + tmp_view + "), will re-acquire the state");
            try {
                jChannel.getState(tmp_view.getCoord(), 30000);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        } else {
            System.out.println("Member of the new primary partition (" + tmp_view + "), will do nothing");
        }
    }
}
