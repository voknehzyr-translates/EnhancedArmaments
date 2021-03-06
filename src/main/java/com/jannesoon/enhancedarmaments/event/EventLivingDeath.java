package com.jannesoon.enhancedarmaments.event;

import com.jannesoon.enhancedarmaments.config.Config;
import com.jannesoon.enhancedarmaments.essentials.Ability;
import com.jannesoon.enhancedarmaments.essentials.Experience;
import com.jannesoon.enhancedarmaments.util.EAUtils;
import com.jannesoon.enhancedarmaments.util.NBTHelper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Updates weapon information when killing a target with a valid weapon. Used to update experience,
 * level, abilities, and so on.
 *
 */
@Mod.EventBusSubscriber
public class EventLivingDeath
{
	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event)
	{
		if (event.getSource().getTrueSource() instanceof EntityPlayer && !(event.getSource().getTrueSource() instanceof FakePlayer))
		{
			EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
			
			ItemStack stack;
			if(EventLivingHurt.bowfriendlyhand == null)
				stack = player.getHeldItem(player.getActiveHand());
			else
				stack = player.getHeldItem(EventLivingHurt.bowfriendlyhand);
			
			if (stack != ItemStack.EMPTY && EAUtils.canEnhanceMelee(stack.getItem()))
			{
				NBTTagCompound nbt = NBTHelper.loadStackNBT(stack);
				
				if (nbt != null)
					if(nbt.hasKey("EA_ENABLED"))
					{
						if (Ability.ETHEREAL.hasAbility(nbt))
						{
							player.inventory.getCurrentItem().setDamage((player.inventory.getCurrentItem().getDamage() - (Ability.ETHEREAL.getLevel(nbt)*2)));
						}
						addBonusExperience(event, nbt);
						updateLevel(player, stack, nbt);
						NBTHelper.saveStackNBT(stack, nbt);
					}
			}
			else if (stack != ItemStack.EMPTY && EAUtils.canEnhanceRanged(stack.getItem()))
			{
				NBTTagCompound nbt = NBTHelper.loadStackNBT(stack);

				if (nbt != null)
					if(nbt.hasKey("EA_ENABLED"))
					{
						if (Ability.ETHEREAL.hasAbility(nbt))
						{
							player.inventory.getCurrentItem().setDamage((player.inventory.getCurrentItem().getDamage() - (Ability.ETHEREAL.getLevel(nbt)*2+1)));
						}
						addBonusExperience(event, nbt);
						updateLevel(player, stack, nbt);
					}
			}
		}
		else if (event.getSource().getTrueSource() instanceof EntityArrow)
		{
			EntityArrow arrow = (EntityArrow) event.getSource().getTrueSource();
			
			if (EAUtils.getEntityByUniqueId(arrow.shootingEntity) instanceof EntityPlayer && EAUtils.getEntityByUniqueId(arrow.shootingEntity) != null)
			{
				EntityPlayer player = (EntityPlayer) EAUtils.getEntityByUniqueId(arrow.shootingEntity);
				if (player != null)
				{
					ItemStack stack = player.inventory.getCurrentItem();

					if (stack != ItemStack.EMPTY)
					{
						NBTTagCompound nbt = NBTHelper.loadStackNBT(stack);

						if (nbt != null)
						{
							addBonusExperience(event, nbt);
							updateLevel(player, stack, nbt);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Called everytime a target dies. Adds bonus experience based on how much health the target had.
	 * @param event
	 * @param nbt
	 */
	private static void addBonusExperience(LivingDeathEvent event, NBTTagCompound nbt)
	{
		if (Experience.getLevel(nbt) < Config.maxLevel)
		{
			if (event.getEntityLiving() != null)
			{
				EntityLivingBase target = event.getEntityLiving();
				int bonusExperience = 0;
				
				if (target.getMaxHealth() < 10) bonusExperience = 3;
				else if (target.getMaxHealth() > 9 && target.getMaxHealth() < 20) bonusExperience = 6;
				else if (target.getMaxHealth() > 19 && target.getMaxHealth() < 50) bonusExperience = 15;
				else if (target.getMaxHealth() > 49 && target.getMaxHealth() < 100) bonusExperience = 50;
				else if (target.getMaxHealth() > 99) bonusExperience = 70;
				
				Experience.setExperience(nbt, Experience.getExperience(nbt) + bonusExperience);
			}
		}
	}
	
	/**
	 * Called everytime a target dies. Used to update the level of the weapon.
	 * @param player
	 * @param stack
	 * @param nbt
	 */
	private static void updateLevel(EntityPlayer player, ItemStack stack, NBTTagCompound nbt)
	{
		int level = Experience.getNextLevel(player, stack, nbt, Experience.getLevel(nbt), Experience.getExperience(nbt));
		Experience.setLevel(nbt, level);
	}
}