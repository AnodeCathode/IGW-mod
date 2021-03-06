package igwmod;

import igwmod.lib.Constants;
import igwmod.network.NetworkHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = Constants.MOD_ID, name = "In-Game Wiki Mod")
public class IGWMod{
    @SidedProxy(clientSide = "igwmod.ClientProxy", serverSide = "igwmod.ServerProxy")
    public static IProxy proxy;

    @Instance(Constants.MOD_ID)
    public IGWMod instance;

    /**
     * This method is used to reject connection when the server has server info available for IGW-mod. Unless the properties.txt explicitly says
     * it's okay to connect without IGW-Mod, by setting "optional=true".
     * @param installedMods
     * @param side
     * @return
     */
    @NetworkCheckHandler
    public boolean onConnectRequest(Map<String, String> installedMods, Side side){
        if(side == Side.SERVER || proxy == null) return true;
        File serverFolder = new File(proxy.getSaveLocation() + File.separator + "igwmod" + File.separator);
        if(serverFolder.exists() || new File(proxy.getSaveLocation() + File.separator+ "igwmodServer" + File.separator).exists()) {//TODO remove legacy
            String str = proxy.getSaveLocation() + File.separator+ "igwmod" + File.separator+ "properties.txt";
            File file = new File(str);
            if(!file.exists()) {
                file = new File(proxy.getSaveLocation() + File.separator + "igwmodServer" + File.separator + "properties.txt");//TODO remove legacy
            }
            if(file.exists()) {
                try {
                    FileInputStream stream = new FileInputStream(file);
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                    List<String> textList = new ArrayList<String>();
                    String line = br.readLine();
                    while(line != null) {
                        textList.add(line);
                        line = br.readLine();
                    }
                    br.close();

                    if(textList != null) {
                        for(String s : textList) {
                            String[] entry = s.split("=");
                            if(entry[0].equals("optional")) {
                                if(Boolean.parseBoolean(entry[1])) return true;
                            }
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            String version = installedMods.get(Constants.MOD_ID);
            if(version.equals("${version}")) return true;
            return version != null && version.equals(Constants.fullVersionString());
        } else {
            return true;
        }

    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        event.getModMetadata().version = Constants.fullVersionString();
        proxy.preInit(event);
        NetworkHandler.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event){

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        proxy.postInit();
    }

    @EventHandler
    public void processIMCRequests(FMLInterModComms.IMCEvent event){
        proxy.processIMC(event);
    }
}
