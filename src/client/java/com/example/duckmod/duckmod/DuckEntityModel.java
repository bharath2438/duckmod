package com.example.duckmod.duckmod;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class DuckEntityModel<T extends Entity> extends AnimalModel<T> {
	private final ModelPart head;
	private final ModelPart neck;
	private final ModelPart beak;
	private final ModelPart body;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	public DuckEntityModel(ModelPart root) {
		super(false, 10.0F, 0.0F);
		this.head = root.getChild("head");
		this.beak = root.getChild("beak");
		this.body = root.getChild("body");
		this.leftLeg = root.getChild("left_leg");
		this.rightLeg = root.getChild("right_leg");
		this.leftWing = root.getChild("left_wing");
		this.rightWing = root.getChild("right_wing");
		this.neck = root.getChild("neck");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData head = modelPartData.addChild("head", ModelPartBuilder.create().uv(0, 33).cuboid(-2.0F, -23.0F, -5.0F, 4.0F, 5.0F, 4.0F, new Dilation(0.0F))
				.uv(54, 20).cuboid(-2.0F, -22.0F, -6.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F))
				.uv(17, 34).cuboid(-2.0F, -22.0F, -1.0F, 4.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData beak = modelPartData.addChild("beak", ModelPartBuilder.create().uv(37, 32).cuboid(-2.0F, -20.0F, -9.0F, 4.0F, 1.0F, 3.0F, new Dilation(0.0F))
				.uv(42, 13).cuboid(-1.0F, -19.0F, -8.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -13.0F, -7.0F, 8.0F, 7.0F, 13.0F, new Dilation(0.0F))
				.uv(14, 41).cuboid(-3.0F, -11.0F, 6.0F, 6.0F, 6.0F, 2.0F, new Dilation(0.0F))
				.uv(45, 26).cuboid(-2.0F, -12.0F, 8.0F, 4.0F, 3.0F, 1.0F, new Dilation(0.0F))
				.uv(23, 20).cuboid(-3.0F, -14.0F, -6.0F, 6.0F, 1.0F, 10.0F, new Dilation(0.0F))
				.uv(0, 20).cuboid(-3.0F, -6.0F, -5.0F, 6.0F, 2.0F, 11.0F, new Dilation(0.0F))
				.uv(50, 26).cuboid(-4.0F, -6.0F, -2.0F, 1.0F, 2.0F, 5.0F, new Dilation(0.0F))
				.uv(50, 26).cuboid(3.0F, -6.0F, -2.0F, 1.0F, 2.0F, 5.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData left_leg = modelPartData.addChild("left_leg", ModelPartBuilder.create().uv(45, 19).cuboid(-1.0F, 0.0F, 0.0F, 1.0F, 5.0F, 2.0F, new Dilation(0.0F))
				.uv(2, 51).cuboid(-2.0F, 5.0F, -1.0F, 3.0F, 1.0F, 3.0F, new Dilation(0.0F))
				.uv(37, 0).cuboid(-2.0F, 5.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
				.uv(37, 3).cuboid(-1.0F, 5.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
				.uv(37, 0).cuboid(0.0F, 5.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(3.0F, 18.0F, 0.0F));

		ModelPartData right_leg = modelPartData.addChild("right_leg", ModelPartBuilder.create().uv(23, 20).cuboid(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 2.0F, new Dilation(0.0F))
				.uv(40, 9).cuboid(-1.0F, 5.0F, -1.0F, 3.0F, 1.0F, 3.0F, new Dilation(0.0F))
				.uv(37, 0).cuboid(-1.0F, 5.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
				.uv(37, 0).cuboid(1.0F, 5.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F))
				.uv(37, 3).cuboid(0.0F, 5.0F, -2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-3.0F, 18.0F, 0.0F));

		ModelPartData left_wing = modelPartData.addChild("left_wing", ModelPartBuilder.create().uv(28, 31).cuboid(0.0F, 0.0F, -6.0F, 1.0F, 6.0F, 6.0F, new Dilation(0.0F))
				.uv(0, 20).cuboid(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 4.0F, new Dilation(0.0F))
				.uv(0, 42).cuboid(0.0F, 0.0F, 4.0F, 1.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(4.0F, 11.0F, 0.0F));

		ModelPartData right_wing = modelPartData.addChild("right_wing", ModelPartBuilder.create().uv(29, 0).cuboid(-1.0F, 0.0F, -6.0F, 1.0F, 6.0F, 6.0F, new Dilation(0.0F))
				.uv(0, 0).cuboid(-1.0F, 0.0F, 0.0F, 1.0F, 5.0F, 4.0F, new Dilation(0.0F))
				.uv(29, 0).cuboid(-1.0F, 0.0F, 4.0F, 1.0F, 4.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-4.0F, 11.0F, 0.0F));

		ModelPartData neck = modelPartData.addChild("neck", ModelPartBuilder.create().uv(39, 40).cuboid(-2.0F, -18.0F, -4.0F, 4.0F, 4.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	protected Iterable<ModelPart> getHeadParts() {
		return ImmutableList.of(this.head, this.beak);
	}

	protected Iterable<ModelPart> getBodyParts() {
		return ImmutableList.of(this.body, this.rightLeg, this.leftLeg, this.rightWing, this.leftWing, this.neck);
	}

	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		this.head.pitch = headPitch * 0.002753292F;
		this.head.yaw = headYaw * 0.010453292F;
		this.beak.pitch = this.head.pitch;
		this.beak.yaw = this.head.yaw;
		this.neck.pitch = this.head.pitch;
		this.neck.yaw = this.head.yaw;
		this.rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 0.4F * limbDistance;
		this.leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 0.4F * limbDistance;
		this.rightWing.roll = -animationProgress*1.25F;
		this.leftWing.roll = animationProgress*1.25F;
	}
}