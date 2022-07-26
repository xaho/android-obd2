/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package nl.xaho.javaobd.OBDCommands;

import com.github.pires.obd.commands.PercentageObdCommand;
import com.github.pires.obd.enums.AvailableCommandNames;

/**
 * Calculated Engine Load value.
 *
 */
public class InstantLoadCommand extends PercentageObdCommand {

    /**
     * <p>Constructor for LoadCommand.</p>
     */
    public InstantLoadCommand() {
        super("01 04 1");
    }

    /**
     * <p>Constructor for LoadCommand.</p>
     *
     * @param other a {@link InstantLoadCommand} object.
     */
    public InstantLoadCommand(InstantLoadCommand other) {
        super(other);
    }

    /*
     * (non-Javadoc)
     *
     * @see pt.lighthouselabs.obd.commands.ObdCommand#getName()
     */
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.ENGINE_LOAD.getValue();
    }

}
