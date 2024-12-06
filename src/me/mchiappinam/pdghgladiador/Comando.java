package me.mchiappinam.pdghgladiador;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Comando implements CommandExecutor {
	private Main plugin;
	public Comando(Main main) {
		plugin=main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("gladiador")) {
			if(args.length==0) {
				if(sender==plugin.getServer().getConsoleSender()) {
					sender.sendMessage("�3�l[Gladiador] �cConsole bloqueado de executar o comando!");
					return true;
				}
				if(plugin.getGladiadorEtapa()==0) {
					sender.sendMessage("�3�l[Gladiador] �cO evento gladiador n�o est� acontecendo!");
					return true;
				}
				if(plugin.getGladiadorEtapa()>1) {
					sender.sendMessage("�3�l[Gladiador] �cO evento gladiador j� come�ou!");
					return true;
				}
				if(plugin.participantes.contains(sender.getName())) {
					sender.sendMessage("�3�l[Gladiador] �cVoc� j� entrou no evento gladiador!");
					return true;
				}
				if(plugin.getConfig().contains("Bans."+sender.getName().toLowerCase())) {
					sender.sendMessage("�3�l[Gladiador] �cVoc� est� banido do evento gladiador!");
					sender.sendMessage("�3�l[Gladiador] �cBanido por "+plugin.getConfig().getString("Bans."+sender.getName().toLowerCase()+".Por")+" em "+plugin.getConfig().getString("Bans."+sender.getName().toLowerCase()+".Data"));
					return true;
				}
				if(plugin.core1.getClanManager().getClanPlayer((Player)sender)==null) {
					sender.sendMessage("�3�l[Gladiador] �cVoc� n�o tem clan!");
					return true;
				}
                if(((Player)sender).isInsideVehicle()) {
				     sender.sendMessage("�3�l[Gladiador] �cVoc� est� dentro de um ve�culo!");
				     return true;
				}
                if(((Player)sender).isDead()) {
				     sender.sendMessage("�3�l[Gladiador] �cVoc� est� morto!");
				     return true;
				}
        		plugin.clearInv((Player)sender);
				plugin.addPlayer((Player)sender);
				return true;
			}
			else {
				if(args[0].equalsIgnoreCase("sair")) {
					if(plugin.getGladiadorEtapa()==0) {
						sender.sendMessage("�3�l[Gladiador] �cO evento gladiador n�o est� aberto!");
						return true;
					}
					if(plugin.getGladiadorEtapa()!=1) {
						sender.sendMessage("�3�l[Gladiador] �cVoc� n�o pode sair agora!");
						return true;
					}
					plugin.removePlayer((Player)sender,0);
					return true;
				}
				if(args[0].equalsIgnoreCase("camarote")) {
					if(!sender.hasPermission("gladiador.camarote")) {
						sender.sendMessage("�3�l[Gladiador] �cVoc� n�o tem permiss�o para executar esse comando!");
						return true;
					}
					if(plugin.getGladiadorEtapa()==0) {
						sender.sendMessage("�3�l[Gladiador] �cO evento gladiador n�o est� acontecendo!");
						return true;
					}
					if(plugin.participantes.contains(sender.getName())) {
						sender.sendMessage("�3�l[Gladiador] �cVoc� n�o pode ir para o camarote participando do evento gladiador!");
						return true;
					}
					((Player)sender).teleport(plugin.camarote);
					sender.sendMessage("�3�l[Gladiador] �oVoc� foi para o camarote do Gladiador!");
					return true;
				}
				//outro cmds, admin!
				if(!sender.hasPermission("gladiador.admin")) {
					sender.sendMessage("�3�l[Gladiador] �cVoc� n�o tem permiss�o para executar esse comando!");
					return true;
				}
				if(args[0].equalsIgnoreCase("forcestart")) {
					if(plugin.getGladiadorEtapa()!=0) {
						sender.sendMessage("�3�l[Gladiador] �cJ� existe um evento gladiador sendo executado!");
						return true;
					}
					if(plugin.getGladiadorEtapa()==0&&!plugin.canStart) {
						sender.sendMessage("�3�l[Gladiador] �cUm evento gladiador est� sendo finalizado!");
						return true;
					}
					sender.sendMessage("�3�l[Gladiador] �oEvento gladiador sendo iniciado!");
					plugin.prepareGladiador();
					return true;
				}
				if(args[0].equalsIgnoreCase("forcestop")) {
					if(plugin.getGladiadorEtapa()==0) {
						sender.sendMessage("�3�l[Gladiador] �cN�o h� nenhum evento gladiador sendo executado!");
						return true;
					}
					plugin.cancelGladiador();
					sender.sendMessage("�3�l[Gladiador] �oEvento gladiador sendo parado!");
					return true;
				}
				if(args[0].equalsIgnoreCase("kick")) {
					if(args.length<2) {
						sender.sendMessage("�3�l[Gladiador] �c/gladiador kick <nome>");
						return true;
					}
					String nome = args[1].toLowerCase();
					Player p = plugin.getServer().getPlayer(nome);
					if(p==null) {
						sender.sendMessage("�3�l[Gladiador] �cJogador n�o encontrado!");
						return true;
					}
					plugin.removePlayer(p, 3);
					sender.sendMessage("�3�l[Gladiador] �o"+nome+" foi kickado do evento gladiador!");
					return true;
				}
				if(args[0].equalsIgnoreCase("info")) {
					if(plugin.getGladiadorEtapa()!=3) {
						sender.sendMessage("�3�l[Gladiador] �cO evento gladiador n�o est� acontecendo!");
						return true;
					}
					sender.sendMessage("�3�l[Gladiador] Restam "+plugin.getClansParticipando().size()+" clans e "+plugin.participantes.size()+" jogadores dentro do gladiador!");
					return true;
				}
				if(args[0].equalsIgnoreCase("ban")) {
					if(args.length<2) {
						sender.sendMessage("�3�l[Gladiador] �c/gladiador ban <nome>");
						return true;
					}
					String nome = args[1].toLowerCase();
					plugin.getConfig().set("Bans."+nome+".Por", sender.getName());
					plugin.getConfig().set("Bans."+nome+".Data", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
					plugin.saveConfig();
					Player p = plugin.getServer().getPlayerExact(nome);
					if(p!=null)
						plugin.removePlayer(p, 3);
					sender.sendMessage("�3�l[Gladiador] �o"+nome+" foi banido dos eventos gladiadores!");
					return true;
				}
				if(args[0].equalsIgnoreCase("unban")) {
					if(args.length<2) {
						sender.sendMessage("�3�l[Gladiador] �c/gladiador ban <nome>");
						return true;
					}
					String nome = args[1].toLowerCase();
					if(!plugin.getConfig().contains("Bans."+nome)) {
						sender.sendMessage("�3�l[Gladiador] �cNome n�o encontrado!");
						return true;
					}
					plugin.getConfig().set("Bans."+nome, null);
					plugin.saveConfig();
					sender.sendMessage("�3�l[Gladiador] �o"+nome+" foi desbanido dos eventos gladiadores!");
					return true;
				}
				if(args[0].equalsIgnoreCase("setspawn")) {
					if(sender==plugin.getServer().getConsoleSender()) {
						sender.sendMessage("�3�l[Gladiador] �cConsole bloqueado de executar o comando!");
						return true;
					}
					Player p = (Player)sender;
					plugin.spawn=p.getLocation();
					plugin.getConfig().set("Arena.Entrada", plugin.spawn.getWorld().getName()+";"+plugin.spawn.getX()+";"+plugin.spawn.getY()+";"+plugin.spawn.getZ()+";"+plugin.spawn.getYaw()+";"+plugin.spawn.getPitch());
					plugin.saveConfig();
					sender.sendMessage("�3�l[Gladiador] �oSpawn marcado!");
					return true;
				}
				if(args[0].equalsIgnoreCase("setsaida")) {
					if(sender==plugin.getServer().getConsoleSender()) {
						sender.sendMessage("�3�l[Gladiador] �cConsole bloqueado de executar o comando!");
						return true;
					}
					Player p = (Player)sender;
					plugin.saida=p.getLocation();
					plugin.getConfig().set("Arena.Saida", plugin.saida.getWorld().getName()+";"+plugin.saida.getX()+";"+plugin.saida.getY()+";"+plugin.saida.getZ()+";"+plugin.saida.getYaw()+";"+plugin.saida.getPitch());
					plugin.saveConfig();
					sender.sendMessage("�3�l[Gladiador] �oSa�da marcada!");
					return true;
				}
				if(args[0].equalsIgnoreCase("setcamarote")) {
					if(sender==plugin.getServer().getConsoleSender()) {
						sender.sendMessage("�3�l[Gladiador] �cConsole bloqueado de executar o comando!");
						return true;
					}
					Player p = (Player)sender;
					plugin.camarote=p.getLocation();
					plugin.getConfig().set("Arena.Camarote", plugin.camarote.getWorld().getName()+";"+plugin.camarote.getX()+";"+plugin.camarote.getY()+";"+plugin.camarote.getZ()+";"+plugin.camarote.getYaw()+";"+plugin.camarote.getPitch());
					plugin.saveConfig();
					sender.sendMessage("�3�l[Gladiador] �oCamarote marcado!");
					return true;
				}
				if(args[0].equalsIgnoreCase("reload")) {
					if(plugin.getGladiadorEtapa()!=0) {
						sender.sendMessage("�3�l[Gladiador] �cH� um evento gladiador acontecendo!");
						return true;
					}
					plugin.reloadConfig();
					String ent[] = plugin.getConfig().getString("Arena.Entrada").split(";");
					plugin.spawn = new Location(plugin.getServer().getWorld(ent[0]),Double.parseDouble(ent[1]),Double.parseDouble(ent[2]),Double.parseDouble(ent[3]),Float.parseFloat(ent[4]),Float.parseFloat(ent[5]));
					String sai[] = plugin.getConfig().getString("Arena.Saida").split(";");
					plugin.saida = new Location(plugin.getServer().getWorld(sai[0]),Double.parseDouble(sai[1]),Double.parseDouble(sai[2]),Double.parseDouble(sai[3]),Float.parseFloat(sai[4]),Float.parseFloat(sai[5]));
					String cam[] = plugin.getConfig().getString("Arena.Camarote").split(";");
					plugin.camarote = new Location(plugin.getServer().getWorld(cam[0]),Double.parseDouble(cam[1]),Double.parseDouble(cam[2]),Double.parseDouble(cam[3]),Float.parseFloat(cam[4]),Float.parseFloat(cam[5]));
					sender.sendMessage("�3�l[Gladiador] �oConfigura��o recarregada!");
					return true;
				}
				sendHelp((Player)sender);
			}
			return true;
		}
		if(cmd.getName().equalsIgnoreCase("gladiadores")) {
			if(args.length==0) {
				if(sender==plugin.getServer().getConsoleSender()) {
					sender.sendMessage("�3�l[Gladiador] �cConsole bloqueado de executar o comando!");
					return true;
				}
				sender.sendMessage("�4�l[PDGH] �2- �3�l[Gladiador]");
				if(plugin.getConfig().getString("Vencedores") == "[]") {
					sender.sendMessage("�cAtualmente ningu�m est� com a TAG �6[Gladiador]");
				}else{
					sender.sendMessage("�6�l"+plugin.getConfig().getString("Vencedores").replace("[", "").replace("]", "").replace(", ", " �3e �6�l"));
				}
					 return true;
			}
		}
		return true;
	}
	
	private void sendHelp(Player p) {
		p.sendMessage("�d�lPDGHGladiador - Comandos do plugin:");
		p.sendMessage("�2/gladidor ? -�a- Lista de comandos");
		p.sendMessage("�c/gladidor forcestart -�a- For�a o inicio do evento gladiador");
		p.sendMessage("�c/gladidor forcestop -�a- For�a a parada do evento gladiador");
		p.sendMessage("�2/gladidor kick <nome> -�a- Kicka um jogador do evento gladiador");
		p.sendMessage("�2/gladidor ban <nome> -�a- Bane um jogador do evento gladiador");
		p.sendMessage("�2/gladidor unban <nome> -�a- Desbane um jogador do evento gladiador");
		p.sendMessage("�2/gladidor setspawn -�a- Marca local de spawn do evento gladiador");
		p.sendMessage("�2/gladidor setsaida -�a- Marca local de saida do evento gladiador");
		p.sendMessage("�2/gladidor setcamarote -�a- Marca local do camarote do evento gladiador");
		p.sendMessage("�2/gladidor setcamarote -�a- Mostra quantos jogadores est�o dentro do evento gladiador");
		p.sendMessage("�c/gladidor reload -�a- Recarrega a configura��o");
	}

}
