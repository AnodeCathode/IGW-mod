package igwmod.gui;

import igwmod.IGWMod;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class LocatedTexture implements IReservedSpace, IWidget{
    public ResourceLocation texture;
    public int x, y, width, height;
    private int textureId;
    private boolean useAlpha;
    private double alpha;

    public LocatedTexture(ResourceLocation texture, int x, int y, int width, int height){
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if(texture.getResourcePath().startsWith("server")) {
            try {
                BufferedImage image = ImageIO.read(new FileInputStream(new File(IGWMod.proxy.getSaveLocation() + File.separator + "igwmod" + File.separator + texture.getResourcePath().substring(7))));
                DynamicTexture t = new DynamicTexture(image);
                textureId = t.getGlTextureId();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public LocatedTexture(ResourceLocation texture, int x, int y){
        this(texture, x, y, 1);
    }

    public LocatedTexture(ResourceLocation texture, int x, int y, double scale){
        this(texture, x, y, 0, 0);
        try {
            BufferedImage bufferedimage;
            if(texture.getResourcePath().startsWith("server")) {
                bufferedimage = ImageIO.read(new FileInputStream(new File(IGWMod.proxy.getSaveLocation() + File.separator + "igwmod" + File.separator + texture.getResourcePath().substring(7))));
            } else {
                IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(texture);
                InputStream inputstream = iresource.getInputStream();
                bufferedimage = ImageIO.read(inputstream);
            }
            width = (int)(bufferedimage.getWidth() * scale);
            height = (int)(bufferedimage.getHeight() * scale);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public LocatedTexture setAlpha(double alpha){
        this.alpha = alpha;
        useAlpha = true;
        return this;
    }

    @Override
    public Rectangle getReservedSpace(){
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void renderBackground(GuiWiki gui, int mouseX, int mouseY){
        if(texture.getResourcePath().startsWith("server")) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        } else {
            gui.mc.getTextureManager().bindTexture(texture);
        }
        if(useAlpha) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4d(1, 1, 1, alpha);
        }
        drawTexture(x, y, width, height);
        if(useAlpha) {
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4d(1, 1, 1, 1);
        }
    }

    @Override
    public void renderForeground(GuiWiki gui, int mouseX, int mouseY){}

    public static void drawTexture(int x, int y, int width, int heigth){
        int minYCap = Math.max(0, GuiWiki.MIN_TEXT_Y - y);
        int maxYCap = Math.min(heigth, GuiWiki.MAX_TEXT_Y - y);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + maxYCap, 0, 0.0, (float)maxYCap / heigth);//TODO render at right Z level
        tessellator.addVertexWithUV(x + width, y + maxYCap, 0, 1.0, (float)maxYCap / heigth);
        tessellator.addVertexWithUV(x + width, y + minYCap, 0, 1, (float)minYCap / heigth);
        tessellator.addVertexWithUV(x, y + minYCap, 0, 0, (float)minYCap / heigth);
        tessellator.draw();
        // this.drawTexturedModalRect(x, y, 0, 0, 16, 16);
    }

    @Override
    public void setX(int x){
        this.x = x;
    }

    @Override
    public void setY(int y){
        this.y = y;
    }

    @Override
    public int getX(){
        return x;
    }

    @Override
    public int getY(){
        return y;
    }

    @Override
    public int getHeight(){
        return height;
    }
}
