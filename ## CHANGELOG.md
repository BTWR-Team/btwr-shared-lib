## UNFINISHED
+ [WIP] General code cleanup: Refactored a lot of mixin class names and interface names from the .api package for better readability and self-explanation of what they're used for

## v0.8.6
+ Added a new API event "ANIMAL_STARTLE_TICK" for animals being startled. Other mods can hook into it and define behavior
+ Added a new util class "SuffocationUtil" which has methods for checking extended cases for when a mob is suffocated

## v0.8.5
+ Added a new class "DisabledRecipeTypeRegistry" which allows specifying recipes by type or identifier to disable entirely. Works with vanilla and custom recipes as well
+ Updated the mod to Fabric API 0.116.15 & Fabric Loader 0.19.3

## v0.8.4
+ Added a new registry class "HeadDropRegistry". It's used for handling default mob head drop pairs of entity type and item (skull/head item), but can also be used by addon makers/modders to add compatibility for their custom mobs to drop custom skull items so they are compatible with BTWR's Head dropping logic which is mainly handled by BTWR: Core, but also for BWT: Hardcore Tweak's chopping block dropping mechanic too
+ Moved certain registry classes to the .api package
+ Updated the mod to Fabric API 0.116.11 & Fabric Loader 0.19.2

## v0.8.3
+ Added an item stack result builder in the ExtendedShapelessRecipe class for making results that have item stack data instead of just items

## v0.8.2
+ Added a new JsonBuilder class 'ShapedRecipeWithStackJsonBuilder' for easier creation of recipes that have itemstacks for their recipe results
+ Added methods to RecipeUtils for easier recipe removal of Animageddon and Self-Sustainable
+ Updated the mod to Fabric API 0.116.9 & Fabric Loader 0.18.4

## v0.8.1
+ Fixed progressive crafting items progress bar to fill up at the correct amounts

## v0.8
### Added
+ RepairRecipeBlockedRegistry — allows mods to block specific items or item classes from being combined/repaired in the crafting grid, grindstone, and/or anvil, with per-operation granularity
+ The ability for blocks to provide custom behavior when destroyed on fire via the "btwr_onDestroyedByFire" and the "btwr_hasCustomFireDestructionBehavior" methods
+ Functionality for falling blocks which allow to calculate the distance a block would fall. They are accessed through the IFallingImpactData interface
+ A bunch of new methods from BTW into Block.class for hardpoint, ground cover and fire-related functionalities
+ A new method from BTW into World.class for checking if a block has a solid top surface
+ A new class "FireBlockUtils" which contains common utility methods related to fire blocks and similar functionality
+ A new WorldUtils class for utility methods related to the world for current functionality at the time of development
+ A green plus to show under the crafting result slot which shows additional drops when a recipe that has those is used
+ A custom use action for progressive crafting items and its related rendering changes when an item with that use action is used
+ A new class "GroundCoverBlock". Currently this is used only in Sturdy Trees, but it might prove useful in the future for different cases in other mods
+ Some new classes in the mod that aren't used currently but may prove useful in the feature

### Moved / Changed
+ Mixin accessor classes for loot pool related methods from other mods into this one for shared usage
+ The ProgressiveCraftingItem class from Self-Sustainable to this mod so it can be used for other mods which will provide other progressive crafting item implementations
+ A bunch of classes and added methods to classes from Self-Sustainable in this mod for future-proofing use cases
+ Some loot pool accessor classes from other mods of mine in this mod for future-proofing use cases

## v0.7
+ Added a new class BTWREvents which holds common use cases events. Right now it only has the LIVING_TICK event which adds a hook for adding tick-specific code to all living entities
+ Changed how directional drops are handled. Each mod that uses directional drops must now register their use cases per ItemStack used and BlockState used against via the DirectionalDropConditions registry class.
+ Fixed an issue with RecipeUtils where the ofBTWR() method was not working properly
+ Refactored a bunch of packages internally into the .api package

## v0.6.5
+ Added a few new classes in the .api package for creating configuration library settings via TOML

## v0.6.4(0.64)
+ Added a new class "ServerChunkGenerateEvents" which has block replacement events on chunk generation.
+ Changed the mod versioning scheme from "0.xx" to "0.x.x" for the new beta releases. This should help translate naturally into the 1.0.0 numbering scheme once the mod is ready for "Release" versioning
+ Removed the old "BlockReplacementRegistry" class because it wasn't working properly. This is now handled with the "ServerChunkGenerateEvents" which is properly using Fabric API events instead.

## v0.63
+ The mod is now officially in Beta!
+ Added comments with explanations for many tags in BTWRConventionalTags
+ Added penalty text compatibility from PenaltyDisplayManager with the "Leave My Bars Alone" mod
+ Added penalty text compatibility from PenaltyDisplayManager with the "Tough As Nails" mod
+ Added several new common tags for items and blocks in BTWRConventionalTags for strata and tool-based checks
+ Added a new HudYOffsetRegistry class in PenaltyDisplayManager which can be used to specify what Y offset of the penalty text should be. This is useful for addon authors if they want to add compatibility with their mods
+ Added a new interface class "EntityAttachmentBase" which provides default methods to implement when attaching data to entities
+ Added a new class "UpdateRequiringData" which makes easier the process of adding data attachments which need to be marked for update (marked dirty) 
+ Added a new class "SimpleEvent" which allows easier creation of custom events
+ Added authors(ivangeevo, jeffinitup) to the mod files, so they should display properly in places where they should be displayed
+ Changed mod license in the source code and in public releases to be MIT
+ Refactored pretty much the whole code; mainly for readability and cleaning up, but also so it's more in order with other mods from the BTWR project
+ Removed unused item and block tags in BTWRConventionalTags

# Start of beta versions

---

# End of alpha versions

## v0.62
+ Added methods in Block.class related to animal graze functionality for usage in other mods from the BTWR Project or any other mods that might want to use them
+ Updated the mod to Fabric Loader 0.17.3

## v0.61
+ Fixed a bug that would increment item stack count of ingredients used in ExtendedShapelessRecipe when crafting
+ Fixed a bug that made tools not get destroyed when their durability reaches 0 with the ExtendedShapelessRecipe
+ Updated the mod to Fabric API 0.116.7

## v0.60
+ Renamed the CraftingWithTool Recipe to ExtendedShapelessRecipe to better represent what it does
+ Added EMI support for extended shapeless recipes to properly show up and also show what additional drops there are if present

## v0.59
+ Added a new type of recipe "TOOL_SHAPELESS_CRAFTING" that handles shapeless crafting with tools. They can have additional drops and damage a tool on crafting specified. This is mainly used for recipes like crafting logs with different axes or leather with shears, etc.
+ Added two new item tags "CRAFTING_WITH_TOOLS_ITEMS" & "DAMAGE_ON_CRAFTING_TOOLS" to be used for specifying which tools can be used in the new Tool Crafting Recipe.
+ Removed old mixin modifications to recipe remainder which caused incompatibilities and bugs with other recipes like "repair_item". Recipe remainder for damaging tools is now handled in the ToolCraftingShapelessRecipe instead.

## v0.58
+ Added remainder logic that damages certain item stacks when used in crafting instead of consuming them (shears and axes)
+ Updated the mod to Fabric API 0.116.6 & Fabric Loader 0.17.2

## v0.57
+ Added the following common block tags: "LOOSEN_ON_IMPROPER_BREAK" & "LOOSEN_ON_IMPROPER_BREAK_SLABS" for blocks that would loosen neighbors when broken with an improper tool. This is mainly used in Tough Environment to change neighbouring blocks to Loose Dirt.

## v0.56
+ Added a common block tag "ALWAYS_FERTILE_SOIL" that targets farmland-like blocks that can grow plants without requiring moisture.
+ Updated the mod to Fabric API 0.116.4

## v0.55
+ Added a new sound in the CraftingSoundConfig "FIZZ_SOUND", which is essentially the "Fire Extinguish" sound but tweaked with pitch and volume
+ Removed the item tag for items that can only stack up to 16 because it wasn't applying properly.
+ Updated the mod to Fabric API 0.116.1

## v0.54
+ Added a new item tag for items that can only stack up to 16
+ Updated the mod to Fabric API 0.116.0 & Fabric Loader 0.16.14

## v0.53
+ Cleanup old hoe functionality changes in favor of moving them to BTWR: Core version 0.31
+ Updated the mod to Fabric API 0.115.6 & Fabric Loader 0.16.13

## v0.52
+ Added a new common block tag "FARMLAND_VIABLE_DIRT" that defines which (dirt) blocks can be converted to farmland when broken with BTWR:Core's alternative hoe usage mechanic.
+ Added a new common block tag "FARMLAND_VIABLE_GRASS" that defines which (grass) blocks can be converted to dirt on hoe left click break (same as above functionality)

## v0.51
+ Add config options and added compatibility for PenaltyDisplayManager (jeffyjamzhd)
+ Fix game breaking error with CraftingResultSlotMixin (jeffyjamzhd)

## v0.50
### Penalty Display System (jeffyjamzhd)

+ Added a unified penalty registration system to standardize rendering across mods.
+ Introduced HUDInitializeListener for registering penalties via EventHUDInitialized.register(listener).
+ Added addPenalty(penalty) and removePenalty(penalty) methods for managing penalties dynamically.
+ Implemented priority-based rendering with condition lambdas for flexibility.

## v0.49
+ Better world generation replacement logic that preserves blockstate properties of original block and apply them to the new one if possible
+ Updated the mod to Fabric API 0.115.3

## v0.48
+ Added a utility class 'BlockReplacementRegistry' for easy block replacement during(before) world gen applies the original block. This allows to set custom blocks during world generation.
+ Fixed a bug that made blocks drop directionally for mods that didn't support these blocks. (Will only work with Tough Environment and Sturdy Trees for now)

## v0.47
+ Added a missin mixin code for making shears actually break blocks efficiently

## v0.46
+ Added a SHEARS_EFFICIENT block tag for adding blocks that shear items can efficiently break.

## v0.45
+ Fixed cobblestone missing from the COBBLESTONE_CRAFTING_MATERIALS tag
+ Updated to Fabric API 0.115.0 & Fabric Loader 0.16.10

## v0.44
+ Added a Block method in the mod API to check if a block is fertilized for growing plants ("getIsFertilizedForPlantGrowth")

## v0.43
+ Added a Block method in the mod API to check if another block is attached to the facing of a block ("isBlockAttachedToFacing").

## v0.42
+ Added a common block tag "WOODEN_MISC_BLOCKS" for modded blocks that are considered "wooden"
+ Fixed a bug where the game would crash when (non-player) entities tried to destroy blocks and drop stacks

## v0.41
+ Added Shears to the IRON_TOOLS item tag, which in turn makes it make the proper metallic sound when crafted.
+ Improved the crafting sound code even more and made sure it works in shift-click item retrieval as well.
+ Moved the crafting sounds of tools/items to be added by BTWR: Core.

## v0.40
+ Improved the code for the crafting sounds for items & made that a separate system.
+ Tried fixing mod version check failing problem on older versions by removing "SNAPSHOT" in the string that checks the version in the code.
+ Updated to Fabric API 0.114.0

## v0.39-SNAPSHOT
+ Changed the btwr-ds mod id to be btwr_ds in some places that had the old one.

## v0.38-SNAPSHOT
+ Added missing block tag entry for directional dropping blocks for the oak log block

## v0.37-SNAPSHOT
+ Conventional item tags changes (same but fixes)
+ Fixed item tag translations to display properly

## v0.36-SNAPSHOT
+ Conventional item tags changes

## v0.35-SNAPSHOT
- Added sounds to play on craft for tools (wooden, stone, metalic types)
- Added new item tags for adding sounds to modded tools like mentioned above
- Improved the crafting sounds for other misc items and added sound to play for items that get crafted with shears

## v0.34-SNAPSHOT
- Added shared conventional tag for meats viable for sandwich food.
- Added conventional block tag name translations
- Changed the sounds for crafting for Slime-like items to be louder

## v0.33-SNAPSHOT
- Improved conditions for placement for PlaceableAsBlock items. They are now not placeable on leaves

## v0.32-SNAPSHOT
- Added missing mixins for BlockAdded related functionality & Made hoe breaking logic work properly without needing BTWR:Core

## v0.31-SNAPSHOT
- Added ExtendedShapelessRecipe which allows adding new items to existing shapeless recipes to drop as side drops

## v0.29-SNAPSHOT
- Added translations for all BTWR conventional tags

## v0.27-SNAPSHOT
- Removed changes from previous version

## v0.26-SNAPSHOT
- Added Extended Shapeless Recipe with .additionalDrops method

## v0.22-SNAPSHOT
- Added WEB_BLOCKS block tag for cobweb and web block from animageddon

## v0.18-SNAPSHOT
- Testing improved directional drop logic injection

## v0.17-SNAPSHOT
- Added Item tag for AXES_CAN_BREAK_PLANKS
- Added datagen & generated all tags that are exclusively adding vanilla things in this mod, and the rest that are BTWR
exclusive items/block were left in the BTWR: core mod

## v0.14-SNAPSHOT
- Improved code for RecipeProviderUtils class

## v0.13-SNAPSHOT
- Added more methods to RecipeProviderUtils class

## v0.12-SNAPSHOT
- Reworked the directional dropping injection code to be less intrusive


## v0.11-SNAPSHOT
- Added RecipeProviderUtils class
- Refactored the StackDroppingManager class to better inject its code without breaking the original implementation

## v0.10-SNAPSHOT
- Added Disabled Recipe type

## v0.4-SNAPSHOT
- Added PlaceableAsBlock logic.

## v0.2-SNAPSHOT
- Moved BTWRConventionalTags to this library mod

## v0.1-SNAPSHOT
Initial release
- Added BlockAdded interface
- Added PlayerEntityAdded interface

# Start of alpha versions