package fr.cs.orekit.attitudes;

import org.apache.commons.math.geometry.Vector3D;

import fr.cs.orekit.bodies.BodyShape;
import fr.cs.orekit.bodies.GeodeticPoint;
import fr.cs.orekit.frames.Frame;
import fr.cs.orekit.time.AbsoluteDate;
import fr.cs.orekit.utils.PVCoordinates;


/**
 * This class handles target pointing attitude law.

 * <p>
 * This class represents the attitude law where the satellite z axis is
 * pointing to a ground point target.</p>
 * <p>
 * The target position and velocity are defined in a body frame specified by the user. 
 * It is important to make sure this frame is consistent.
 * </p>
 * <p>
 * The object <code>TargetPointing</code> is guaranteed to be immutable.
 * </p>
 * @see     GroundPointing
 * @version $Id:OrbitalParameters.java 1310 2007-07-05 16:04:25Z luc $
 * @author  V. Pommier
 */
public class TargetPointing extends GroundPointing {

    /** Serializable UID. */
    private static final long serialVersionUID = -6066660334639100843L;

    /** Target in body frame. */
    private final PVCoordinates target;


    /** Creates a new instance from body frame and target expressed in position/velocity coordinates.
     * @param bodyFrame Body frame.
     * @param target Target expressed in position/velocity coordinates in body frame
     */
    public TargetPointing(Frame bodyFrame, PVCoordinates target) {
        super(bodyFrame);
        this.target = target;
    }

    /** Creates a new instance from body shape and target expressed in geodetic coordinates.
     * @param --
     */
    public TargetPointing(Frame bodyFrame, GeodeticPoint targetGeo, BodyShape shape) {
        super(bodyFrame);
        
        /* Transform target from geodetic coordinates to position-velocity coordinates */
        Vector3D target = shape.transform(targetGeo);
        this.target = new PVCoordinates(target, Vector3D.zero);
    }

    /** Get target expressed in body frame at given date.
     * @param date Date for computing.
     * @param pv Satellite position-velocity vector at given date in given frame.
     * @param frame Frame in which satellite position-velocity is given.
     * 
     * <p>User should check that position/velocity and frame is consistent with given frame.
     * </p>
     */
    protected PVCoordinates getTargetInBodyFrame(AbsoluteDate date,
                                                 PVCoordinates pv, Frame frame) {
        /* Returns attribute target */
        return target;
    }

}
