package net.studioxai.studioxBe.domain.image.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MotionType {
    ZOOM_IN("Apply a smooth Zoom In effect on the main subject."),
    ZOOM_OUT("Apply a smooth Zoom Out effect from the main subject."),
    ROTATE_LEFT("Rotate the camera to the Left around the subject."),
    ROTATE_RIGHT("Rotate the camera to the Right around the subject."),
    ROTATE_UP("Rotate the camera Upwards looking at the subject."),
    ROTATE_DOWN("Rotate the camera Downwards looking at the subject."),
    MOVE_UP("Move the camera smoothly Upwards."),
    MOVE_DOWN("Move the camera smoothly Downwards.");

    private final String promptInstruction;
}
