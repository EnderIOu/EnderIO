package gg.galaxygaming.gasconduits.common.conduit.advanced;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.DyeColor;
import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NNList.NNIterator;
import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vertex;

import crazypants.enderio.base.conduit.ConnectionMode;
import crazypants.enderio.base.conduit.IClientConduit;
import crazypants.enderio.base.conduit.IClientConduit.WithDefaultRendering;
import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.IConduitBundle;
import crazypants.enderio.base.conduit.IConduitTexture;
import crazypants.enderio.base.conduit.geom.CollidableComponent;
import crazypants.enderio.conduits.render.BakedQuadBuilder;
import crazypants.enderio.conduits.render.ConduitBundleRenderManager;
import crazypants.enderio.conduits.render.ConduitInOutRenderer;
import crazypants.enderio.conduits.render.DefaultConduitRenderer;
import gg.galaxygaming.gasconduits.client.utils.GasRenderUtil;
import mekanism.api.gas.GasStack;

public class AdvancedGasConduitRenderer extends DefaultConduitRenderer {

    @Override
    public boolean isRendererForConduit(@Nonnull IConduit conduit) {
        return conduit instanceof AdvancedGasConduit;
    }

    @Override
    public boolean canRenderInLayer(@Nonnull WithDefaultRendering con, @Nonnull BlockRenderLayer layer) {
        return super.canRenderInLayer(con, layer) || layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    protected void addConduitQuads(@Nonnull IConduitBundle bundle, @Nonnull IClientConduit conduit,
                                   @Nonnull IConduitTexture tex, @Nonnull CollidableComponent component,
                                   float selfIllum, BlockRenderLayer layer, @Nonnull List<BakedQuad> quads) {
        super.addConduitQuads(bundle, conduit, tex, component, selfIllum, layer, quads);
        ConduitInOutRenderer.renderIO(bundle, conduit, component, layer, quads, DyeColor.RED, DyeColor.RED);

        if (component.isCore() || component.data != null) {
            return;
        }
        AdvancedGasConduit lc = (AdvancedGasConduit) conduit;
        GasStack gas = lc.getGasType();
        @Nonnull
        TextureAtlasSprite texture = gas != null && gas.getGas() != null && gas.amount > 0 ?
                GasRenderUtil.getStillTexture(gas) : lc.getNotSetEdgeTexture();

        // FIXME this logic is duplicated from DefaultConduitRenderer
        float shrink = 1 / 32f;
        EnumFacing componentDirection = component.getDirection();
        float xLen = Math.abs(componentDirection.getXOffset()) == 1 ? 0 : shrink;
        float yLen = Math.abs(componentDirection.getYOffset()) == 1 ? 0 : shrink;
        float zLen = Math.abs(componentDirection.getZOffset()) == 1 ? 0 : shrink;

        BoundingBox cube = component.bound;
        BoundingBox bb = cube.expand(-xLen, -yLen, -zLen);

        List<Vertex> vertices = new ArrayList<>();
        for (NNIterator<EnumFacing> itr = NNList.FACING.fastIterator(); itr.hasNext();) {
            EnumFacing dir = itr.next();
            if (dir != componentDirection && dir != componentDirection.getOpposite()) {

                EnumFacing vDir = RenderUtil.getVDirForFace(dir);
                if (componentDirection == EnumFacing.UP || componentDirection == EnumFacing.DOWN) {
                    vDir = RenderUtil.getUDirForFace(dir);
                } else if ((componentDirection == EnumFacing.NORTH || componentDirection == EnumFacing.SOUTH) &&
                        dir.getYOffset() != 0) {
                            vDir = RenderUtil.getUDirForFace(dir);
                        }

                float minU = texture.getMinU();
                float maxU = texture.getMaxU();
                float minV = texture.getMinV();
                float maxV = texture.getMaxV();

                double sideScale = Math.max(bb.sizeX(), bb.sizeY()) * 2 / 16f;
                sideScale = Math.max(sideScale, bb.sizeZ() * 2 / 16f);
                double width = Math.min(bb.sizeX(), bb.sizeY()) * 15f / 16f;

                List<Vertex> corners = bb.getCornersWithUvForFace(dir, minU, maxU, minV, maxV);
                moveEdgeCorners(corners, vDir, width);
                moveEdgeCorners(corners, componentDirection.getOpposite(), sideScale);
                vertices.addAll(corners);

                corners = bb.getCornersWithUvForFace(dir, minU, maxU, minV, maxV);
                moveEdgeCorners(corners, vDir.getOpposite(), width);
                moveEdgeCorners(corners, componentDirection.getOpposite(), sideScale);
                vertices.addAll(corners);

            }
        }
        BakedQuadBuilder.addBakedQuads(quads, vertices, texture, null);

        if (conduit.getConnectionMode(component.getDirection()) == ConnectionMode.DISABLED) {
            TextureAtlasSprite tex2 = ConduitBundleRenderManager.instance.getConnectorIcon(component.data);
            List<Vertex> corners = component.bound.getCornersWithUvForFace(component.getDirection(), tex2.getMinU(),
                    tex2.getMaxU(), tex2.getMinV(), tex2.getMaxV());
            List<Vertex> vertices2 = new ArrayList<>(corners);
            // back face
            for (int i = corners.size() - 1; i >= 0; i--) {
                Vertex c = corners.get(i);
                vertices2.add(c);
            }

            BakedQuadBuilder.addBakedQuads(quads, vertices2, tex2, null);
        }
    }

    private void moveEdgeCorners(List<Vertex> vertices, EnumFacing edge, double scaleFactor) {
        int[] indices = getClosest(edge, vertices);
        vertices.get(indices[0]).xyz.x -= scaleFactor * edge.getXOffset();
        vertices.get(indices[1]).xyz.x -= scaleFactor * edge.getXOffset();
        vertices.get(indices[0]).xyz.y -= scaleFactor * edge.getYOffset();
        vertices.get(indices[1]).xyz.y -= scaleFactor * edge.getYOffset();
        vertices.get(indices[0]).xyz.z -= scaleFactor * edge.getZOffset();
        vertices.get(indices[1]).xyz.z -= scaleFactor * edge.getZOffset();
    }

    private int[] getClosest(EnumFacing edge, List<Vertex> vertices) {
        int[] res = new int[] { -1, -1 };
        boolean highest = edge.getXOffset() > 0 || edge.getYOffset() > 0 || edge.getZOffset() > 0;
        double minMax = highest ? -Double.MAX_VALUE : Double.MAX_VALUE;
        int index = 0;
        for (Vertex v : vertices) {
            double val = get(v.xyz, edge);
            if (highest ? val >= minMax : val <= minMax) {
                if (val != minMax) {
                    res[0] = index;
                } else {
                    res[1] = index;
                }
                minMax = val;
            }
            index++;
        }
        return res;
    }

    private double get(Vector3d xyz, EnumFacing edge) {
        if (edge == EnumFacing.EAST || edge == EnumFacing.WEST) {
            return xyz.x;
        }
        if (edge == EnumFacing.UP || edge == EnumFacing.DOWN) {
            return xyz.y;
        }
        return xyz.z;
    }
}
