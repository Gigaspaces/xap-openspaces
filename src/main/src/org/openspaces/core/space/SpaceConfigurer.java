package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;

/**
 * A generic interface for space configurers.
 *
 * @author kimchy
 */
public interface SpaceConfigurer {

    IJSpace space();
}
