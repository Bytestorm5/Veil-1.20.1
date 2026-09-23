package foundry.veil.mixin.pipeline.client;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.MatrixStack;
import org.joml.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Deque;

@Mixin(PoseStack.class)
public abstract class PipelinePoseStackMixin implements MatrixStack {

    @Unique
    private static final Matrix3f veil$IDENTITY_NORMAL = new Matrix3f();

    @Shadow
    @Final
    private Deque<PoseStack.Pose> poseStack;

    @Shadow
    public abstract PoseStack.Pose last();

    @Unique
    private final Quaternionf veil$castQuat = new Quaternionf();

    @Override
    public void clear() {
        while (this.poseStack.size() > 1) {
            ((PoseStack) (Object) this).popPose();
        }
        this.setIdentity();
    }

    @Override
    public void translate(float x, float y, float z) {
        this.pose().pose().translate(x, y, z);
    }

    @Override
    public void rotate(Quaterniondc rotation) {
        ((PoseStack) (Object) this).mulPose(this.veil$castQuat.set(rotation));
    }

    @Override
    public void rotate(Quaternionfc rotation) {
        ((PoseStack) (Object) this).mulPose(this.veil$castQuat.set(rotation));
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        ((PoseStack) (Object) this).mulPose(this.veil$castQuat.identity().rotateAxis(angle, x, y, z));
    }

    @Override
    public void rotateXYZ(float x, float y, float z) {
        ((PoseStack) (Object) this).mulPose(this.veil$castQuat.identity().rotateXYZ(x, y, z));
    }

    @Override
    public void rotateZYX(float z, float y, float x) {
        ((PoseStack) (Object) this).mulPose(this.veil$castQuat.identity().rotateZYX(z, y, x));
    }

    @Override
    public void rotateAround(Quaterniondc rotation, double x, double y, double z) {
        ((PoseStack) (Object) this).rotateAround(this.veil$castQuat.set(rotation), (float) x, (float) y, (float) z);
    }

    @Override
    public void rotateAround(Quaternionfc rotation, float x, float y, float z) {
        ((PoseStack) (Object) this).rotateAround(this.veil$castQuat.set(rotation), x, y, z);
    }

    @Override
    public void applyScale(float x, float y, float z) {
        ((PoseStack) (Object) this).scale(x, y, z);
    }

    @Override
    public boolean isIdentity() {
        PoseStack.Pose pose = this.pose();
        return (pose.pose().properties() & Matrix4fc.PROPERTY_IDENTITY) != 0 && pose.normal().equals(veil$IDENTITY_NORMAL);
    }

    @Override
    public boolean isEmpty() {
        return this.poseStack.size() == 1;
    }

    @Override
    public void matrixPush() {
        ((PoseStack) (Object) this).pushPose();
    }

    @Override
    public void matrixPop() {
        ((PoseStack) (Object) this).popPose();
    }

    @Override
    public PoseStack.Pose pose() {
        return this.poseStack.getLast();
    }

    @Override
    public PoseStack toPoseStack() {
        return (PoseStack) (Object) this;
    }
}
