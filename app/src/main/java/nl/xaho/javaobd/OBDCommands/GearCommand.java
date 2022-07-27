package nl.xaho.javaobd.OBDCommands;

import com.github.pires.obd.commands.ObdCommand;

public class GearCommand extends ObdCommand {
    int ratio;

    public GearCommand() {
        super("01 A4");
    }

    @Override
    protected void performCalculations() {
        int c = buffer.get(3);
        int d = buffer.get(4);
        ratio = (256 * c + d) / 1000;
    }

    @Override
    public String getFormattedResult() {
        return String.valueOf(ratio);
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(ratio);
    }

    @Override
    public String getName() {
        return "RPM Command";
    }
}
