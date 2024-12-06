package me.mchiappinam.pdghgladiador;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
//import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;

public class Listeners implements Listener {
	private Main plugin;
	public Listeners(Main main) {
		plugin=main;
	}

	@EventHandler
	private void onDeath(PlayerDeathEvent e) {
		if(e.getEntity().getKiller() instanceof Player) {
			Player killer = e.getEntity().getKiller();
			if(plugin.participantes.contains(killer.getName())&&plugin.participantes.contains(e.getEntity().getName())) {
				int k = plugin.totalParticipantes.get(killer.getName());
				plugin.totalParticipantes.remove(killer.getName());
				plugin.totalParticipantes.put(killer.getName(), k+1);
				killer.sendMessage("§3§l[Gladiador] §eVocê matou "+e.getEntity().getName()+" (total = "+(k+1)+")");
			}
		}
		plugin.removePlayer(e.getEntity(),1);
		plugin.checkGladiadorEnd();
	}
	
	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		/**if(plugin.participantes.contains(e.getPlayer().getName())) {
			if(plugin.getGladiadorEtapa()!=1) {
				plugin.getServer().broadcastMessage("§3§l[Gladiador] §c§l"+e.getPlayer().getName()+" §csaiu no meio do evento gladiador e tomou ban.");
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ban "+e.getPlayer().getName()+" 600 DC no evento gladiador §3§l[Gladiador]§c");
			}
		}*/
		plugin.removePlayer(e.getPlayer(),2);
		if(plugin.sb.getTeam(e.getPlayer().getName().toLowerCase())!=null) {
			plugin.sb.getTeam(e.getPlayer().getName().toLowerCase()).unregister();
		}
	}
	
	@EventHandler
	private void onKick(PlayerKickEvent e) {
		/**if(plugin.participantes.contains(e.getPlayer().getName())) {
		if(plugin.getGladiadorEtapa()!=1) {
			plugin.getServer().broadcastMessage("§3§l[Gladiador] §c§l"+e.getPlayer().getName()+" §csaiu no meio do evento gladiador e tomou ban.");
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ban "+e.getPlayer().getName()+" 600 DC no evento gladiador §3§l[Gladiador]§c");
		}
	}*/
		plugin.removePlayer(e.getPlayer(),2);
		if(plugin.sb.getTeam(e.getPlayer().getName().toLowerCase())!=null) {
			plugin.sb.getTeam(e.getPlayer().getName().toLowerCase()).unregister();
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	private void onDamage(EntityDamageByEntityEvent e) {
		if(plugin.getGladiadorEtapa()!=0)
			if(e.getEntity() instanceof Player)
				if(e.getDamager() instanceof Player||e.getDamager() instanceof Projectile) {
					Player ent = (Player)e.getEntity();
					Player dam = null;
					if(e.getDamager() instanceof Player)
						dam=(Player)e.getDamager();
					else {
						Projectile a = (Projectile) e.getDamager();
						if(a.getShooter() instanceof Player)
							dam=(Player)a.getShooter();
					}
					if(plugin.participantes.contains(ent.getName()))
						if(plugin.getGladiadorEtapa()!=3) {
							e.setCancelled(true);
							if(dam!=null)
								dam.sendMessage("§3§l[Gladiador] §4PvP desativado no momento!");
						}
					if(dam!=null&&plugin.getGladiadorEtapa()==3)
						if(plugin.participantes.contains(ent.getName())&&plugin.participantes.contains(dam.getName())) {
							if(plugin.core1.getClanManager().getClanPlayer(ent).getClan()==plugin.core1.getClanManager().getClanPlayer(dam).getClan())
								e.setCancelled(true);
							else if(plugin.core1.getClanManager().getClanPlayer(ent).getClan()!=plugin.core1.getClanManager().getClanPlayer(dam).getClan())
								e.setCancelled(false);
						}
				}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	private void onDamageP(PotionSplashEvent e) {
		for(Entity ent2 : e.getAffectedEntities())
			if(ent2 instanceof Player)
				if(plugin.getGladiadorEtapa()!=0) {
					Player ent = (Player)ent2;
					Player dam = null;
					if(e.getPotion().getShooter() instanceof Player)
						dam=(Player)e.getEntity().getShooter();
					if(plugin.participantes.contains(ent.getName()))
						if(plugin.getGladiadorEtapa()!=3) {
							e.setCancelled(true);
							if(dam!=null)
								dam.sendMessage("§3§l[Gladiador] §4PvP desativado no momento!");
						}
					if(dam!=null&&plugin.getGladiadorEtapa()==3)
						if(plugin.participantes.contains(ent.getName())&&plugin.participantes.contains(dam.getName())) {
							if(plugin.core1.getClanManager().getClanPlayer(ent).getClan()==plugin.core1.getClanManager().getClanPlayer(dam).getClan()) {
								e.getAffectedEntities().remove(ent2);
							}
						}
				}
	}
	
	/*@EventHandler(priority = EventPriority.HIGHEST)
	private void onChat(AsyncPlayerChatEvent e) {
		if(plugin.getConfig().getStringList("Vencedores").contains(e.getPlayer().getName().toLowerCase())) {
			//String[] s = e.getFormat().split("<");
			//e.setFormat(s[0]+"<"+plugin.getConfig().getString("Premios.Tag").replaceAll("&", "§")+s[1]);
			e.getPlayer().setDisplayName(plugin.getConfig().getString("Premios.Tag").replaceAll("&", "§")+(plugin.core1.getClanManager().getClanPlayer(e.getPlayer())==null?"":plugin.core1.getClanManager().getClanPlayer(e.getPlayer()).getTagLabel())+e.getPlayer().getName());
		}
	}*/

	@EventHandler(priority=EventPriority.HIGHEST)
	private void onChat(ChatMessageEvent e) {
		if(plugin.getConfig().getStringList("Vencedores").contains(e.getSender().getName().toLowerCase())) {
			e.setTagValue("gladiador", "§6[Gladiador]");
		}
	}
	
	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		if(plugin.sb.getTeam(e.getPlayer().getName().toLowerCase())!=null) {
			plugin.sb.getTeam(e.getPlayer().getName().toLowerCase()).unregister();
		}
		if(plugin.core1.getClanManager().getClanPlayer(e.getPlayer().getName())!=null&&plugin.sb.getTeam(e.getPlayer().getName().toLowerCase())==null) {
			Team t = plugin.sb.registerNewTeam(e.getPlayer().getName().toLowerCase());
			//t.setPrefix(plugin.formatTag(e.getPlayer()));
			t.addPlayer(e.getPlayer());
		}
		

		/*if(plugin.getConfig().getStringList("Vencedores").contains(e.getPlayer().getName().toLowerCase())) {
			//String[] s = e.getFormat().split("<");
			//e.setFormat(s[0]+"<"+plugin.getConfig().getString("Premios.Tag").replaceAll("&", "§")+s[1]);
			e.getPlayer().setDisplayName(plugin.getConfig().getString("Premios.Tag").replaceAll("&", "§")+(plugin.core1.getClanManager().getClanPlayer(e.getPlayer())==null?"":plugin.core1.getClanManager().getClanPlayer(e.getPlayer()).getTagLabel())+e.getPlayer().getName());
		}*/
		
	}
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPCmd(PlayerCommandPreprocessEvent e) {
		if(plugin.getGladiadorEtapa()!=0) {
			if(e.getMessage().toLowerCase().startsWith("/clan")) {
				if(!e.getMessage().toLowerCase().startsWith("/clan create")&&!e.getMessage().toLowerCase().startsWith("/clan invite")) {
					e.getPlayer().sendMessage("§c§lComando bloqueado no evento gladiador!");
					e.getPlayer().sendMessage("§2§lOs únicos comandos liberados são: §b§l/clan create §2§le §b§l/clan invite");
					e.setCancelled(true);
				}
			}
			if(e.getMessage().toLowerCase().startsWith("/ret")) {
				e.getPlayer().sendMessage("§c§lComando bloqueado no evento gladiador!");
				e.setCancelled(true);
			}else if(e.getMessage().toLowerCase().startsWith("/bring")) {
				e.getPlayer().sendMessage("§c§lComando bloqueado no evento gladiador!");
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent e) {
		if(plugin.participantes.contains(e.getEntity().getName()))
			e.setCancelled(true);
	}
}
