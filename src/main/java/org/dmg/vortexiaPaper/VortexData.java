package org.dmg.vortexiaPaper;

public class VortexData {
    VortexState state;
    float intensity;
    double theta;
    double orbitTheta;

    VortexData(VortexState state, float intensity) {
        this.state = state;
        this.intensity = intensity;
        this.theta = 0.0;
        this.orbitTheta = 0.0;
    }
}
