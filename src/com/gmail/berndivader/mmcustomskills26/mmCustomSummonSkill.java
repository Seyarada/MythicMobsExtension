package com.gmail.berndivader.mmcustomskills26;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MobManager;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.mobs.entities.MythicEntity;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillString;

public class mmCustomSummonSkill extends SkillMechanic implements ITargetedLocationSkill, ITargetedEntitySkill {
	protected MythicMobs mythicmobs = Main.getPlugin().getMythicMobs();
	protected MobManager mobmanager = this.mythicmobs.getMobManager();
	private MythicMob mm;
	private MythicEntity me;
	private String strType,tag;
	private int amount;
	@SuppressWarnings("unused")
	private boolean yUpOnly, onSurface, inheritThreatTable, copyThreatTable, useEyeDirection, setowner;
	private double noise, yNoise, addx, addy, addz, inFrontBlocks;

	public mmCustomSummonSkill(String skill, MythicLineConfig mlc) {
		super(skill, mlc);
		this.ASYNC_SAFE = false;
		this.strType=mlc.getString(new String[]{"type","t","mob","m"},null);
		this.tag=mlc.getString(new String[]{"addtag","tag"});
		this.noise = mlc.getInteger(new String[] { "noise", "n", "radius", "r" }, 0);
		this.yNoise = mlc.getDouble(new String[] { "ynoise", "yn", "yradius", "yr" }, this.noise);
		this.yUpOnly = mlc.getBoolean(new String[] { "yradiusuponly", "yradiusonlyup", "yruo", "yu" }, false);
		this.onSurface = mlc.getBoolean(new String[] { "onsurface", "os", "s" }, true);
		this.copyThreatTable = mlc.getBoolean(new String[] { "copythreattable", "ctt" }, false);
		this.inheritThreatTable = mlc.getBoolean(new String[] { "inheritthreattable", "itt" }, false);
		this.addx = mlc.getDouble(new String[] { "addx", "ax" }, 0);
		this.addy = mlc.getDouble(new String[] { "addy", "ay" }, 0);
		this.addz = mlc.getDouble(new String[] { "addz", "az" }, 0);
		this.useEyeDirection = mlc.getBoolean(new String[] { "useeyedirection", "eyedirection", "ued" }, false);
		this.inFrontBlocks = mlc.getDouble(new String[] { "inFrontBlocks", "inFront", "ifb" }, 0D);
		this.setowner = mlc.getBoolean(new String[] { "setowner", "so" }, false);
	}

	@Override
	public boolean castAtLocation(SkillMetadata data, AbstractLocation t) {
		return cast(data,t,null);
	}

	@Override
	public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
		return cast(data,target.getLocation(),target);
	}
	
	private boolean cast(SkillMetadata data, AbstractLocation tl, AbstractEntity te) {
		AbstractLocation target = tl.clone();
		if (this.useEyeDirection) {
			target = BukkitAdapter
					.adapt(CustomSkillStuff.getLocationInFront(BukkitAdapter.adapt(target), this.inFrontBlocks));
		}
		target.add(this.addx, this.addy, this.addz);
		if (this.mm == null && this.me == null) {
			this.mm = this.mobmanager.getMythicMob(this.strType);
			if (this.mm == null) {
				this.me = MythicEntity.getMythicEntity(this.strType);
			}
		}
		if (this.mm != null) {
			if (this.noise > 0) {
				for (int i = 1; i <= this.amount; ++i) {
					this.mythicmobs.getMobManager();
					AbstractLocation l = MobManager.findSafeSpawnLocation(target, (int) this.noise, (int) this.yNoise,
							this.mm.getMythicEntity().getHeight(), this.yUpOnly);
					ActiveMob ams = this.mm.spawn(l, data.getCaster().getLevel());
					if (ams == null)
						continue;
					this.mythicmobs.getEntityManager().registerMob(ams.getEntity().getWorld(), ams.getEntity());
					if (this.tag!=null) {
						String tt = SkillString.unparseMessageSpecialChars(this.tag);
						tt = SkillString.parseMobVariables(tt, data.getCaster(), te, data.getTrigger());
						ams.getEntity().addScoreboardTag(tt);
					}
					if (data.getCaster() instanceof ActiveMob) {
						ActiveMob am = (ActiveMob) data.getCaster();
						ams.setParent(am);
						ams.setFaction(am.getFaction());
						if (this.setowner) {
							ams.setOwner(data.getCaster().getEntity().getUniqueId());
						}
						if (this.copyThreatTable) {
							try {
								ams.importThreatTable(am.getThreatTable().clone());
								ams.getThreatTable().targetHighestThreat();
							} catch (CloneNotSupportedException e1) {
								e1.printStackTrace();
							}
							continue;
						}
						if (!this.inheritThreatTable)
							continue;
						ams.importThreatTable(am.getThreatTable());
						ams.getThreatTable().targetHighestThreat();
						continue;
					}
				}
			} else {
				for (int i = 1; i <= this.amount; ++i) {
					ActiveMob ams = this.mm.spawn(target, data.getCaster().getLevel());
					if (ams == null)
						continue;
					this.mythicmobs.getEntityManager().registerMob(ams.getEntity().getWorld(), ams.getEntity());
					if (data.getCaster() instanceof ActiveMob) {
						ActiveMob am = (ActiveMob) data.getCaster();
						ams.setParent(am);
						ams.setFaction(am.getFaction());
						if (this.setowner) {
							ams.setOwner(data.getCaster().getEntity().getUniqueId());
						}
						if (this.copyThreatTable) {
							try {
								ams.importThreatTable(am.getThreatTable().clone());
								ams.getThreatTable().targetHighestThreat();
							} catch (CloneNotSupportedException e1) {
								e1.printStackTrace();
							}
							continue;
						}
						if (!this.inheritThreatTable)
							continue;
						ams.importThreatTable(am.getThreatTable());
						ams.getThreatTable().targetHighestThreat();
						continue;
					}
				}
			}
			return true;
		}
		if (this.me != null) {
			if (this.noise > 0) {
				for (int i = 1; i <= this.amount; ++i) {
					this.mythicmobs.getMobManager();
					AbstractLocation l = MobManager.findSafeSpawnLocation(target, (int) this.noise, (int) this.yNoise,
							this.me.getHeight(), this.yUpOnly);
					this.me.spawn(BukkitAdapter.adapt(l));
				}
			} else {
				for (int i = 1; i <= this.amount; ++i) {
					this.me.spawn(BukkitAdapter.adapt(target));
				}
			}
			return true;
		}
		return true;
	}
	
}
