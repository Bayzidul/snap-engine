package org.esa.beam.glayer;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.Property;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerContext;
import com.bc.ceres.glayer.LayerType;
import com.bc.ceres.glayer.support.ImageLayer;
import com.bc.ceres.glevel.MultiLevelSource;
import org.esa.beam.framework.datamodel.BitmaskDef;
import org.esa.beam.framework.datamodel.BitmaskOverlayInfo;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.RasterDataNode;
import org.esa.beam.glevel.MaskImageMultiLevelSource;

import java.awt.geom.AffineTransform;


/**
 * @author Marco Peters
 * @version $ Revision: $ Date: $
 * @since BEAM 4.6
 * @deprecated since 4.7, use {@link MaskLayerType}
 */
@Deprecated
public class BitmaskLayerType extends ImageLayer.Type {

    public static final String PROPERTY_NAME_BITMASK_DEF = "bitmaskDef";
    public static final String PROPERTY_NAME_PRODUCT = "product";

    @Override
    public String getName() {
        return "Bitmask Layer";
    }

    public static Layer createBitmaskLayer(RasterDataNode raster, final BitmaskDef bitmaskDef,
                                           AffineTransform i2mTransform) {
        final LayerType type = LayerType.getLayerType(BitmaskLayerType.class);
        final PropertyContainer configuration = type.createLayerConfig(null);
        configuration.setValue(BitmaskLayerType.PROPERTY_NAME_BITMASK_DEF, bitmaskDef);
        configuration.setValue(BitmaskLayerType.PROPERTY_NAME_PRODUCT, raster.getProduct());
        configuration.setValue(ImageLayer.PROPERTY_NAME_IMAGE_TO_MODEL_TRANSFORM, i2mTransform);
        final Layer layer = type.createLayer(null, configuration);
        final BitmaskOverlayInfo overlayInfo = raster.getBitmaskOverlayInfo();
        layer.setVisible(overlayInfo != null && overlayInfo.containsBitmaskDef(bitmaskDef));
        return layer;
    }

    @Override
    public Layer createLayer(LayerContext ctx, PropertyContainer configuration) {
        if (configuration.getValue(ImageLayer.PROPERTY_NAME_MULTI_LEVEL_SOURCE) == null) {
            final MultiLevelSource multiLevelSource = createMultiLevelSource(configuration);
            configuration.setValue(ImageLayer.PROPERTY_NAME_MULTI_LEVEL_SOURCE, multiLevelSource);
        }
        final ImageLayer layer = new ImageLayer(this, configuration);
        final BitmaskDef bitmaskDef = (BitmaskDef) configuration.getValue(PROPERTY_NAME_BITMASK_DEF);
        layer.setName(bitmaskDef.getName());
        // TODO: Is this correct? (rq-2009-05-11)
        layer.setTransparency(bitmaskDef.getTransparency());

        return layer;
    }

    private MultiLevelSource createMultiLevelSource(PropertyContainer configuration) {
        final BitmaskDef bitmaskDef = (BitmaskDef) configuration.getValue(PROPERTY_NAME_BITMASK_DEF);
        final Product product = (Product) configuration.getValue(PROPERTY_NAME_PRODUCT);
        final AffineTransform transform = (AffineTransform) configuration.getValue(
                ImageLayer.PROPERTY_NAME_IMAGE_TO_MODEL_TRANSFORM);

        return MaskImageMultiLevelSource.create(product, bitmaskDef.getColor(), bitmaskDef.getExpr(), false, transform);
    }

    @Override
    public PropertyContainer createLayerConfig(LayerContext ctx) {
        final PropertyContainer vc = super.createLayerConfig(ctx);

        vc.addProperty(Property.create(PROPERTY_NAME_BITMASK_DEF, BitmaskDef.class));
        vc.getProperty(PROPERTY_NAME_BITMASK_DEF).getDescriptor().setNotNull(true);

        vc.addProperty(Property.create(PROPERTY_NAME_PRODUCT, Product.class));
        vc.getProperty(PROPERTY_NAME_PRODUCT).getDescriptor().setNotNull(true);

        return vc;
    }

    public Layer createLayer(BitmaskDef bitmaskDef, Product product, AffineTransform i2m) {
        final PropertyContainer configuration = createLayerConfig(null);
        configuration.setValue(PROPERTY_NAME_BITMASK_DEF, bitmaskDef);
        configuration.setValue(PROPERTY_NAME_PRODUCT, product);
        configuration.setValue(ImageLayer.PROPERTY_NAME_IMAGE_TO_MODEL_TRANSFORM, i2m);
        return createLayer(null, configuration);
    }


}