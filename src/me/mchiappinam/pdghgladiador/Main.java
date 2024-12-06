package me.mchiappinam.pdghgladiador;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

//import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;
//import org.bukkit.scoreboard.Team;

public class Main extends JavaPlugin {
	protected SimpleClans core1;
	protected static Economy econ = null;
	protected Scoreboard sb;
	
	private int gladiadorEtapa = 0;
	private int diaAutoStart;
	private int horaAutoStart;
	private int minAutoStart;
	protected boolean canStart = true;
	
	protected Location spawn;
	protected Location saida;
	protected Location camarote;
	
	protected HashMap<String,Integer> totalParticipantes = new HashMap<String,Integer>();
	protected List<String> participantes = new ArrayList<String>();
	protected List<String> vips = new ArrayList<String>();
	
	@Override
    public void onEnable() {
		getServer().getConsoleSender().sendMessage("§3[Gladiador] §2ativando... - Plugin by: mchiappinam");
		getServer().getConsoleSender().sendMessage("§3[Gladiador] §2Acesse: http://pdgh.com.br/");
		
		if(getServer().getPluginManager().getPlugin("SimpleClans")!=null) {
			getServer().getConsoleSender().sendMessage("§2Hooked to SimpleClans 1!");
			core1=(SimpleClans) getServer().getPluginManager().getPlugin("SimpleClans");
		}
		else {
			getLogger().warning("ERRO: SimpleClans nao encontrado!");
			getServer().getPluginManager().disablePlugin(this);
		}
		
		if(!setupEconomy()) {
			getLogger().warning("ERRO: Vault (Economia) nao encontrado!");
			getServer().getPluginManager().disablePlugin(this);
        }
		else
			getServer().getConsoleSender().sendMessage("§2Hooked to Vault (Economia)!");
		
		getServer().getPluginManager().registerEvents(new Listeners(this), this);
		getServer().getPluginCommand("gladiador").setExecutor(new Comando(this));
		getServer().getPluginCommand("gladiadores").setExecutor(new Comando(this));
		
		File file = new File(getDataFolder(),"config.yml");
		if(!file.exists()) {
			try {
				saveResource("config_template.yml",false);
				File file2 = new File(getDataFolder(),"config_template.yml");
				file2.renameTo(new File(getDataFolder(),"config.yml"));
			}
			catch(Exception e) {}
		}
		
		diaAutoStart = Utils.strToCalendar(getConfig().getString("AutoStart.Dia"));
		getServer().getConsoleSender().sendMessage("§2<> Data automatica:");
		getServer().getConsoleSender().sendMessage("§2Dia = "+diaAutoStart);
		horaAutoStart = Integer.parseInt(getConfig().getString("AutoStart.Hora").substring(0,2));
		minAutoStart = Integer.parseInt(getConfig().getString("AutoStart.Hora").substring(2,4));
		getServer().getConsoleSender().sendMessage("§2Hora = "+(horaAutoStart<10?"0"+horaAutoStart:horaAutoStart)+":"+(minAutoStart<10?"0"+minAutoStart:minAutoStart));
		
		String ent[] = getConfig().getString("Arena.Entrada").split(";");
		spawn = new Location(getServer().getWorld(ent[0]),Double.parseDouble(ent[1]),Double.parseDouble(ent[2]),Double.parseDouble(ent[3]),Float.parseFloat(ent[4]),Float.parseFloat(ent[5]));
		String sai[] = getConfig().getString("Arena.Saida").split(";");
		saida = new Location(getServer().getWorld(sai[0]),Double.parseDouble(sai[1]),Double.parseDouble(sai[2]),Double.parseDouble(sai[3]),Float.parseFloat(sai[4]),Float.parseFloat(sai[5]));
		String cam[] = getConfig().getString("Arena.Camarote").split(";");
		camarote = new Location(getServer().getWorld(cam[0]),Double.parseDouble(cam[1]),Double.parseDouble(cam[2]),Double.parseDouble(cam[3]),Float.parseFloat(cam[4]),Float.parseFloat(cam[5]));
		
		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				if(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==diaAutoStart)
					if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)==horaAutoStart)
						if(Calendar.getInstance().get(Calendar.MINUTE)==minAutoStart)
							prepareGladiador();
			}
		}, 0, 700);
		
		sb = getServer().getScoreboardManager().getMainScoreboard();
		
		/**getServer().getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				for(Player p : getServer().getOnlinePlayers()) {
					if(core1.getClanManager().getClanPlayer(p.getName())!=null&&sb.getTeam(p.getName().toLowerCase())==null) {
						Team t = sb.registerNewTeam(p.getName().toLowerCase());
						t.setPrefix(formatTag(p));
						t.addPlayer(p);
					}
					else if(core1.getClanManager().getClanPlayer(p.getName())!=null&&sb.getTeam(p.getName().toLowerCase())!=null) {
						Team t = sb.getPlayerTeam(p);
						t.setPrefix(formatTag(p));
					}
					else if(core1.getClanManager().getClanPlayer(p.getName())==null&&sb.getTeam(p.getName().toLowerCase())!=null) {
						sb.getTeam(p.getName().toLowerCase()).unregister();
					}
				}
			}
		}, getConfig().getInt("Update")*20, getConfig().getInt("Update")*20);*/
	}
	
	@Override
    public void onDisable() {
		getServer().getConsoleSender().sendMessage("§3[Gladiador] §2desativado - Plugin by: mchiappinam");
		getServer().getConsoleSender().sendMessage("§3[Gladiador] §2Acesse: http://pdgh.com.br/");
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	
	
	
	protected void prepareGladiador() {
		String ent[] = getConfig().getString("Arena.Entrada").split(";");
		spawn = new Location(getServer().getWorld(ent[0]),Double.parseDouble(ent[1]),Double.parseDouble(ent[2]),Double.parseDouble(ent[3]),Float.parseFloat(ent[4]),Float.parseFloat(ent[5]));
		String sai[] = getConfig().getString("Arena.Saida").split(";");
		saida = new Location(getServer().getWorld(sai[0]),Double.parseDouble(sai[1]),Double.parseDouble(sai[2]),Double.parseDouble(sai[3]),Float.parseFloat(sai[4]),Float.parseFloat(sai[5]));
		String cam[] = getConfig().getString("Arena.Camarote").split(";");
		camarote = new Location(getServer().getWorld(cam[0]),Double.parseDouble(cam[1]),Double.parseDouble(cam[2]),Double.parseDouble(cam[3]),Float.parseFloat(cam[4]),Float.parseFloat(cam[5]));
		if(gladiadorEtapa!=0)
			return;
		getServer().dispatchCommand(getServer().getConsoleSender(), "simpleclans globalff allow");
		gladiadorEtapa=1;
		tirarTagsAntigas();
		messagePrepare(getConfig().getInt("Timers.Preparar.Avisos"));
	}
	private void messagePrepare(final int vezes) {
		canStart=true;
		if(gladiadorEtapa!=1)
			return;
		canStart=false;
		if(vezes==0)
			preparedGladiador();
		else {
			getServer().broadcastMessage(" ");
			getServer().broadcastMessage("§3§l[Gladiador] §eEvento gladiador automático começando!");
			getServer().broadcastMessage("§3§l[Gladiador] §ePara participar digite: §6§l/gladiador");
			getServer().broadcastMessage("§3§l[Gladiador] §ePremio: §c$"+getConfig().getDouble("Premios.Dinheiro")+"§e, tag §5ⓂⒾⓉⓄ §ee §6[Gladiador]");
			getServer().broadcastMessage("§3§l[Gladiador] §eTempo restante: §c"+vezes*getConfig().getInt("Timers.Preparar.TempoEntre")+" segundos");
			getServer().broadcastMessage("§3§l[Gladiador] §eClans: "+getClansParticipando().size()+" - Jogadores: "+participantes.size());
			getServer().broadcastMessage(" ");
		}
		getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				canStart=true;
				if(gladiadorEtapa!=1)
					return;
				canStart=false;
				messagePrepare(vezes-1);
			}
		}, 20*getConfig().getInt("Timers.Preparar.TempoEntre"));
	}
	
	
	
	
	
	protected void preparedGladiador() {
		if(getClansParticipando().size()<2) {
			cancelGladiador();
			cancelGladiador();
			getServer().broadcastMessage(" ");
			getServer().broadcastMessage("§3§l[Gladiador] §eEvento gladiador automático §cCANCELADO!");
			getServer().broadcastMessage("§3§l[Gladiador] §eMotivo: Quantidade de clans menor que 2");
			getServer().broadcastMessage(" ");
			return;
		}
		gladiadorEtapa=2;
		getServer().broadcastMessage(" ");
		getServer().broadcastMessage("§3§l[Gladiador] §eEvento gladiador sendo INICIADO!");
		getServer().broadcastMessage("§3§l[Gladiador] §eTeleporte para o evento BLOQUEADO!");
		getServer().broadcastMessage(" ");
		canStart=false;
		messageIniciando(getConfig().getInt("Timers.Iniciando.Avisos"));
	}
	private void messageIniciando(final int vezes) {
		canStart=true;
		if(gladiadorEtapa!=2)
			return;
		canStart=false;
		if(vezes==0)
			startGladiador();
		else {
			sendMessageGladiador(" ");
			sendMessageGladiador("§3§l[Gladiador] §eEvento gladiador automático começando!");
			sendMessageGladiador("§3§l[Gladiador] §eTempo inicial para os clans se preparar!");
			sendMessageGladiador("§3§l[Gladiador] §eTempo restante: §c"+vezes*getConfig().getInt("Timers.Iniciando.TempoEntre")+" segundos");
			sendMessageGladiador(" ");
		}
		getServer().getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				canStart=true;
				if(gladiadorEtapa!=2)
					return;
				canStart=false;
				messageIniciando(vezes-1);
			}
		}, 20*getConfig().getInt("Timers.Iniciando.TempoEntre"));
	}
	
	
	
	
	
	protected void startGladiador() {
		canStart=true;
		gladiadorEtapa=3;
		sendMessageGladiador(" ");
		sendMessageGladiador("§3§l[Gladiador] §eVALENDO!");
		sendMessageGladiador("§3§l[Gladiador] §eVALENDO!");
		sendMessageGladiador("§3§l[Gladiador] §eVALENDO!");
		sendMessageGladiador(" ");
	}
	
	
	
	
	
	protected void checkGladiadorEnd() {
		List<Clan> lista = getClansParticipando();
		if(lista.size()==1)
			if(gladiadorEtapa==3) {
				gladiadorEtapa=4;
				Clan vencedor = lista.get(0);
				double premio = (getConfig().getDouble("Premios.Dinheiro")*1.0)/vencedor.getLeaders().size();
				for(ClanPlayer cp : vencedor.getLeaders())
					econ.depositPlayer(cp.getName(), premio);
				String v1 = null;
				int v1_v = -1;
				String v2 = null;
				int v2_v = -1;
				String v3 = null;
				int v3_v = -1;
				for(String n : totalParticipantes.keySet())
					if(core1.getClanManager().getClanByPlayerName(n)==vencedor) {
						int matou = totalParticipantes.get(n);
						if(matou>v1_v) {
							v3_v=v2_v;
							v3=v2;
							v2_v=v1_v;
							v2=v1;
							v1=n;
							v1_v=matou;
						}
						else if(matou>v2_v) {
							v3=v2;
							v3_v=v2_v;
							v2=n;
							v2_v=matou;
						}
						else if(matou>v2_v) {
							v3=n;
							v3_v=matou;
						}
					}
				darMito(v1);
				darTagsNovas(v2,v3);
				getServer().broadcastMessage(" ");
				getServer().broadcastMessage("§3§l[Gladiador] §eEvento gladiador FINALIZADO!");
				getServer().broadcastMessage("§3§l[Gladiador] §eClan vencedor: §l"+vencedor.getName());
				getServer().broadcastMessage("§3§l[Gladiador] §ePremio: §c$"+getConfig().getDouble("Premios.Dinheiro"));
				getServer().broadcastMessage("§3§l[Gladiador] §eTag §5§l[MITO] §epara "+v1+" ("+v1_v+")");
				if(v2!=null)
					getServer().broadcastMessage("§3§l[Gladiador] §eTag §c[Gladiador] §epara "+v2+" ("+v2_v+") "+(v3!=null?"e "+v3+" ("+v3_v+")":""));
				getServer().broadcastMessage(" ");
				sendMessageGladiador("§3§l[Gladiador] §b§lVocê tem "+getConfig().getInt("Timers.Finalizando")+" segundos para recolher os itens do evento Gladiador!");
				getServer().dispatchCommand(getServer().getConsoleSender(), "simpleclans globalff auto");
				getServer().getScheduler().runTaskLater(this, new Runnable() {
					public void run() {
						finalizarGladiador();
					}
				}, 20*getConfig().getInt("Timers.Finalizando"));
			}
	}
	
	
	
	
	protected void finalizarGladiador() {
		sendMessageGladiador(" ");
		sendMessageGladiador("§3§l[Gladiador] §eTempo esgotado!");
		sendMessageGladiador("§3§l[Gladiador] §eFim do evento!");
		sendMessageGladiador(" ");
		cancelGladiador();
	}
	
	protected void darTagsNovas(String v2,String v3) {
		List<String> l = new ArrayList<String>();
		if(v2!=null)
			l.add(v2.toLowerCase());
		if(v3!=null)
			l.add(v3.toLowerCase());
		getConfig().set("Vencedores", l);
		saveConfig();
	}
	
	protected void darMito(String v1) {
		getServer().dispatchCommand(getServer().getConsoleSender(), "setmito "+v1);
	}
	
	protected void tirarTagsAntigas() {
		getConfig().set("Vencedores", new ArrayList<String>());
		saveConfig();
	}
	
	protected void cancelGladiador() {
		if(gladiadorEtapa==0)
			return;
		getServer().dispatchCommand(getServer().getConsoleSender(), "simpleclans globalff auto");
		gladiadorEtapa=0;
		for(String n : participantes) {
			getServer().getPlayer(n).teleport(saida);
		}
		participantes.clear();
		vips.clear();
		totalParticipantes.clear();
	}
	
	protected int getGladiadorEtapa() {
		return gladiadorEtapa;
	}
	
	@SuppressWarnings("deprecation")
	protected void addPlayer(Player p) {
		clearInv(p);
		p.setFoodLevel(20);
		totalParticipantes.put(p.getName(), 0);
		participantes.add(p.getName());
		p.teleport(spawn);
		p.sendMessage(" ");
		p.sendMessage("§3§l[Gladiador] §eVocê entrou no evento gladiador!");
		p.sendMessage("§3§l[Gladiador] §cPara sair digite: §c§l/gladiador sair");
		p.sendMessage("§3§l[Gladiador] §eAgrupe-se com seu clan enquanto o evento está iniciando!");
		p.sendMessage(" ");
		
		clearInv(p);
		Kit(p);
	    p.updateInventory();
	    for(PotionEffect effect : p.getActivePotionEffects()) {
	    	p.removePotionEffect(effect.getType());
	    	p.sendMessage("§3§l[Gladiador] §ePoção §6"+effect.getType().getName()+" §eremovida.");
	    }
	}
	
	public void clearInv(Player p) {
		p.closeInventory();
		p.closeInventory();
		p.closeInventory();
		p.closeInventory();
		p.closeInventory();
		p.getInventory().setHelmet(null);
		p.getInventory().setChestplate(null);
		p.getInventory().setLeggings(null);
		p.getInventory().setBoots(null);
		p.getInventory().clear();
	}
	
	public void Kit(Player p) {
		if(p.hasPermission("pdgh.vip")) {
			ItemStack espada = new ItemStack(Material.DIAMOND_SWORD, 1);
			espada.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
			espada.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
			ItemStack arco = new ItemStack(Material.BOW, 1);
			arco.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE , 5);
			arco.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
			arco.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			arco.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
			ItemStack elmo = new ItemStack(Material.DIAMOND_HELMET, 1);
			elmo.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			elmo.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			ItemStack peito = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
			peito.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			peito.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			ItemStack calca = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
			calca.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			calca.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			ItemStack bota = new ItemStack(Material.DIAMOND_BOOTS, 1);
			bota.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 6);
			bota.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
			p.getInventory().addItem(espada);
			p.getInventory().addItem(arco);
			p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 30, (short) 1));
			p.getInventory().addItem(new ItemStack(Material.POTION, 20, (short) 8233));
			p.getInventory().addItem(new ItemStack(Material.POTION, 20, (short) 8226));
			p.getInventory().addItem(elmo);
			p.getInventory().addItem(peito);
			p.getInventory().addItem(calca);
			p.getInventory().addItem(bota);
			p.getInventory().setHelmet(elmo);
			p.getInventory().setChestplate(peito);
			p.getInventory().setLeggings(calca);
			p.getInventory().setBoots(bota);
			p.getInventory().addItem(new ItemStack(Material.ARROW, 1));
			
			if(!vips.contains(p.getName().toLowerCase())) {
				getServer().broadcastMessage("§3§l[Gladiador] §6§l"+p.getName()+" §eé VIP e ganhou em toda sua armadura +2 leveis de encantamento, +1 armadura completa e o dobro de todas as poções.");
				vips.add(p.getName().toLowerCase());
			}
			
		}else{
		    ItemStack espada = new ItemStack(Material.DIAMOND_SWORD, 1);
		    espada.addEnchantment(Enchantment.DAMAGE_ALL, 5);
		    espada.addEnchantment(Enchantment.FIRE_ASPECT, 2);
		    ItemStack arco = new ItemStack(Material.BOW, 1);
		    arco.addEnchantment(Enchantment.ARROW_DAMAGE , 5);
		    arco.addEnchantment(Enchantment.ARROW_FIRE, 1);
		    arco.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		    arco.addEnchantment(Enchantment.DURABILITY, 3);
		    ItemStack elmo = new ItemStack(Material.DIAMOND_HELMET, 1);
		    elmo.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
		    elmo.addEnchantment(Enchantment.DURABILITY, 3);
		    ItemStack peito = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
		    peito.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
		    peito.addEnchantment(Enchantment.DURABILITY, 3);
		    ItemStack calca = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
		    calca.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
		    calca.addEnchantment(Enchantment.DURABILITY, 3);
		    ItemStack bota = new ItemStack(Material.DIAMOND_BOOTS, 1);
		    bota.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL , 4);
		    bota.addEnchantment(Enchantment.DURABILITY, 3);
		    p.getInventory().addItem(espada);
		    p.getInventory().addItem(arco);
		    p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 30, (short) 1));
		    p.getInventory().addItem(new ItemStack(Material.POTION, 10, (short) 8233));
		    p.getInventory().addItem(new ItemStack(Material.POTION, 10, (short) 8226));
		    p.getInventory().addItem(new ItemStack(Material.ARROW, 1));
			p.getInventory().setHelmet(elmo);
			p.getInventory().setChestplate(peito);
			p.getInventory().setLeggings(calca);
			p.getInventory().setBoots(bota);
		}
	}
	
	protected void removePlayer(Player p,int motive) {//0=sair, 1=morrer, 2=quit, 3=kick
		if(!participantes.contains(p.getName()))
			return;
		participantes.remove(p.getName());
		if(gladiadorEtapa<2)
			totalParticipantes.remove(p.getName());
		else if(gladiadorEtapa==3) {
			if(getClansParticipando().size()>1)
				getServer().broadcastMessage("§3§l[Gladiador] §eRestam "+getClansParticipando().size()+" clans e "+participantes.size()+" jogadores dentro do gladiador!");
			checkGladiadorEnd();
		}
		p.teleport(saida);
		if(gladiadorEtapa==1) {
			totalParticipantes.remove(p.getName());
			if(motive!=3)
				p.sendMessage("§3§l[Gladiador] §eVocê saiu do evento gladiador, para voltar: §c/gladiador");
			else
				p.sendMessage("§3§l[Gladiador] §cVocê foi kickado do evento gladiador");
		}
		else {
			if(motive==0)
				p.sendMessage("§3§l[Gladiador] §eVocê saiu do evento gladiador");
			else if(motive==1)
				p.sendMessage("§3§l[Gladiador] §eVocê morreu no evento gladiador");
			else if(motive==3)
				p.sendMessage("§3§l[Gladiador] §cVocê foi kickado do evento gladiador");
		}
	}
	
	protected List<Clan> getClansParticipando() {
		List<Clan> clans = new ArrayList<Clan>();
		for(String n : participantes) {
			ClanPlayer cp = core1.getClanManager().getClanPlayer(getServer().getPlayer(n));
			if(cp!=null)
				if(!clans.contains(cp.getClan()))
					clans.add(cp.getClan());
		}
		return clans;
	}
	
	protected void sendMessageGladiador(String msg) {
		for(String n : participantes)
			getServer().getPlayer(n).sendMessage(msg);
	}
	
	/**protected String formatTag(Player p) {	
		String final_tag = "";
		ClanPlayer cp = core1.getClanManager().getClanPlayer(p);
		String ctag = cp.getTagLabel();
		String ntag = cp.getTag();
		String lastcor = "";
		int parte = 0;
		for(int i=0;i<ctag.length();i++) {
			char c = cp.getTagLabel().charAt(i);
			if(Character.compare(Character.toLowerCase(c),Character.toLowerCase(ntag.charAt(parte)))==0&&!lastchar(ctag,i)) {
				if(lastcor.equals(ChatColor.getLastColors(ctag.substring(0,i))))
					final_tag+=c;
				else {
					final_tag+=ChatColor.getLastColors(ctag.substring(0,i))+c;
					lastcor=ChatColor.getLastColors(ctag.substring(0,i));
				}
				parte++;
				if(ntag.length()-1<parte)
					break;
			}
		}
		String pronto = getConfig().getString("ClanTag");
		int max = 16-(pronto.replace("%tag%", "").length());
		if(final_tag.length()>max)
			final_tag = final_tag.substring(0,(max-1));
		if(Character.compare(final_tag.charAt(final_tag.length()-1),'§')==0)
			final_tag = final_tag.substring(0,final_tag.length()-1);
		return pronto.replace("%tag%", final_tag).replaceAll("&", "§");
	}
	
	private boolean lastchar(String str,int pos) {
		if(pos==0)
			return false;
		if(Character.compare(str.charAt(pos-1),'§')==0)
			return true;
		return false;
	}*/
	
}
