package foundry.veil.forge;

import foundry.veil.Veil;
import foundry.veil.ext.MinecraftServerExtension;
import foundry.veil.impl.TickTaskSchedulerImpl;
import foundry.veil.impl.command.VeilCommand;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Mod.EventBusSubscriber(modid = Veil.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VeilForgeEvents {

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        TickTaskSchedulerImpl scheduler = ((MinecraftServerExtension) event.getServer()).veil$getScheduler();
        if (scheduler != null) {
            scheduler.run();
        }
    }

    @SubscribeEvent
    public static void serverStopping(ServerStoppingEvent event) {
        TickTaskSchedulerImpl scheduler = ((MinecraftServerExtension) event.getServer()).veil$getScheduler();
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        VeilCommand.register(event.getDispatcher());
    }
}
