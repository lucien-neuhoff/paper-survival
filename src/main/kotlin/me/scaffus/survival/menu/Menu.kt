package me.scaffus.sguis.menu

import me.scaffus.survival.Survival
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

abstract class Menu(
    private val plugin: Survival,
    val name: String,
    private val title: Component,
    private val size: Int,
    private val backgroundItem: ItemStack? = null
) : Listener {
    protected val inventory: Inventory = Bukkit.createInventory(null, size, title)
    val slots: MutableMap<String, Slot> = mutableMapOf()

    init {
        if (backgroundItem != null) setBackground(backgroundItem)
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun open(p: Player) {
        p.openInventory(inventory)
    }

    fun initFromConfig() {
        val menuConfig = plugin.menuManager.getMenuConfig(name)
        menuConfig.let {
            for (slotName in it.getKeys(false)) {
                val slot = it.getInt("slot")

                val displayName = it.getString("name") ?: "<bold>Display Name"
                val material: Material = Material.getMaterial(it.getString("material") ?: "STICK") ?: Material.STICK
                val amount: Int = (it.get("amount") ?: 1) as Int
                val lore: Array<String> = it.getStringList("lore").toTypedArray()
                val itemStack = plugin.helper.createItem(material, amount, displayName, lore)

                val itemSlot = Slot(slotName, slot, itemStack)
                setSlot(itemSlot)
            }
        }
    }

    fun setSlot(slot: Slot) {
        slots[slot.name] = slot
        inventory.setItem(slot.slot, slot.item)
    }

    fun getSlot(slotName: String): Slot? {
        return slots[slotName]
    }

    fun setSlotPosition(slotName: String, position: Int) {
        val slot = getSlot(slotName) ?: return
        slot.slot = position
        setSlot(slot)
    }

    fun setSlotItemStack(slotName: String, itemStack: ItemStack) {
        val slot = getSlot(slotName) ?: return
        slot.item = itemStack
        setSlot(slot)
    }

    fun addSlotAction(slotName: String, action: (p: Player) -> Unit) {
        val slot = getSlot(slotName) ?: return
        if (slot.actions.contains(action)) return
        slot.actions.add(action)
        setSlot(slot)
    }

    fun removeSlotAction(slotName: String, action: (p: Player) -> Unit) {
        val slot = getSlot(slotName) ?: return
        if (!slot.actions.contains(action)) return
        slot.actions.remove(action)
        setSlot(slot)
    }

    protected fun updateSlotLore(slotName: String, lore: Array<String>, vararg placeholders: String) {
        val slot = getSlot(slotName) ?: return

        if (lore.isNotEmpty()) {
            val meta = slot.item.itemMeta
            meta.lore(plugin.helper.formatLore(lore))
            slot.item.itemMeta = meta
        }

        setSlot(slot)
    }


    protected fun updateSlotLoreLine(slotName: String, line: Int, lore: String, vararg placeholders: String) {
        val slot = getSlot(slotName) ?: return
        val itemMeta = slot.item.itemMeta

        if (itemMeta.hasLore()) {
            val itemLore = itemMeta.lore()
            itemLore!![line] = plugin.helper.format(lore, *placeholders)
            itemMeta.lore(itemLore)
        } else {
            val emptyLore: MutableList<Component> = mutableListOf()
            for (i in 0 until line) {
                emptyLore.add(Component.text(""))
            }
            emptyLore.add(line, plugin.helper.format(lore, *placeholders))
            itemMeta.lore(emptyLore)
        }

        setSlot(slot)
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory == inventory) {
            event.isCancelled = true
            val slot = event.rawSlot

            if (slot in 0 until size) {
                handleItemClick(event.whoClicked as Player, slot, event.currentItem)
            }
        }
    }

    protected fun setBackground(item: ItemStack) {
        for (i in 0 until size) {
            inventory.setItem(i, item)
        }
    }

    protected fun handleItemClick(p: Player, slotPosition: Int, item: ItemStack?) {
        slots.values.forEach { slot: Slot ->
            if (slot.slot == slotPosition) slot.actions.forEach { action ->
                action.invoke(
                    p
                )
            }
        }
    }
}