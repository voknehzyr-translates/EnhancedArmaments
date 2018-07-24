package com.jannesoon.enhancedarmaments.event;

import java.util.Random;

import com.jannesoon.enhancedarmaments.config.Config;
import com.jannesoon.enhancedarmaments.leveling.Ability;
import com.jannesoon.enhancedarmaments.leveling.Experience;
import com.jannesoon.enhancedarmaments.leveling.Rarity;
import com.jannesoon.enhancedarmaments.util.NBTHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventLivingUpdate 
{
	private int count=0;
	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event)
	{
		if (event.getEntityLiving() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			
			if (player != null)
			{
				NonNullList<ItemStack> main = player.inventory.mainInventory;
				
				if (!player.world.isRemote)
				{
					for (ItemStack stck : player.inventory.armorInventory)
					{
						if (stck != null && stck.getItem() instanceof ItemArmor)
						{
							NBTTagCompound nbtcompound = stck.getTagCompound();
							float heal=Ability.REMEDIAL.getLevel(nbtcompound);
							if (Ability.REMEDIAL.hasAbility(nbtcompound))
								if(this.count < 120)
								{
									this.count++;
								}
								else
								{
									this.count = 0;
									player.heal(heal);
								}
						}
					}
					for (int i = 0; i < main.size(); i++)
					{
						if (main.get(i) != null)
						{
							Item item = main.get(i).getItem();
							
							if (item instanceof ItemSword || item instanceof ItemAxe || item instanceof ItemHoe || item instanceof ItemArmor || item instanceof ItemBow)
							{
								ItemStack stack = main.get(i);
								NBTTagCompound nbt = NBTHelper.loadStackNBT(stack);

								if (nbt != null)
								{
									if (!Experience.isEnabled(nbt))
									{
										int count = 0;
										
										for (int j = 0; j < Config.itemBlacklist.length; j++)
										{
											if (Config.itemBlacklist[j].equals(stack.getItem().getRegistryName().getResourceDomain() + ":" + stack.getItem().getRegistryName().getResourcePath()))
											{
												count++;
											}
										}
										
										if (count == 0)
										{
											Experience.enable(nbt, true);
											Rarity rarity = Rarity.getRarity(nbt);
											Random rand = player.world.rand;
											
											if (rarity == Rarity.DEFAULT)
											{
												rarity = Rarity.getRandomRarity(rand);
												rarity.setRarity(nbt);
												NBTHelper.saveStackNBT(stack, nbt);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}