package net.sf.anathema.character.main.essencepool.model;

import net.sf.anathema.character.generic.additionalrules.IAdditionalEssencePool;
import net.sf.anathema.character.generic.additionalrules.IAdditionalRules;
import net.sf.anathema.character.generic.character.IMagicCollection;
import net.sf.anathema.character.generic.framework.additionaltemplate.listening.GlobalCharacterChangeAdapter;
import net.sf.anathema.character.generic.framework.additionaltemplate.model.ICharacterModelContext;
import net.sf.anathema.character.generic.framework.essence.IEssencePoolModifier;
import net.sf.anathema.character.generic.magic.ICharm;
import net.sf.anathema.character.generic.magic.IExtendedCharmData;
import net.sf.anathema.character.generic.template.essence.FactorizedTrait;
import net.sf.anathema.character.generic.template.essence.FactorizedTraitSumCalculator;
import net.sf.anathema.character.generic.template.essence.IEssenceTemplate;
import net.sf.anathema.character.generic.template.magic.IGenericCharmConfiguration;
import net.sf.anathema.character.generic.traits.GenericTrait;
import net.sf.anathema.character.generic.traits.types.OtherTraitType;
import net.sf.anathema.character.generic.traits.types.VirtueType;
import net.sf.anathema.character.main.traits.model.TraitMap;
import net.sf.anathema.lib.control.IChangeListener;
import net.sf.anathema.lib.util.IdentifiedInteger;
import org.jmock.example.announcer.Announcer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EssencePoolStrategyImpl implements EssencePoolStrategy {

  private final Announcer<IChangeListener> control = Announcer.to(IChangeListener.class);
  private final IEssenceTemplate essenceTemplate;
  private final IAdditionalRules additionalRules;
  private final TraitMap traitMap;
  private final IMagicCollection magicCollection;
  private final IGenericCharmConfiguration charmConfiguration;
  private final ICharacterModelContext context;

  public EssencePoolStrategyImpl(IEssenceTemplate essenceTemplate, ICharacterModelContext context, TraitMap traitMap,
                                 IMagicCollection magicCollection, IGenericCharmConfiguration charmConfiguration, IAdditionalRules additionalRules) {
    this.traitMap = traitMap;
    this.magicCollection = magicCollection;
    this.charmConfiguration = charmConfiguration;
    this.additionalRules = additionalRules;
    context.getCharacterListening().addChangeListener(new GlobalCharacterChangeAdapter() {
      @Override
      public void changeOccurred() {
        control.announce().changeOccurred();
      }
    });
    this.context = context;
    this.essenceTemplate = essenceTemplate;
  }

  @Override
  public void addPoolChangeListener(IChangeListener listener) {
    control.addListener(listener);
  }

  @Override
  public int getFullPersonalPool() {
    int additionalPool = 0;
    for (IAdditionalEssencePool pool : additionalRules.getAdditionalEssencePools()) {
      additionalPool += pool.getAdditionalPersonalPool(traitMap, magicCollection);
    }
    return getUnmodifiedPersonalPool() + additionalPool;
  }

  @Override
  public int getExtendedPersonalPool() {
    int additionalPool = 0;
    for (IAdditionalEssencePool pool : additionalRules.getAdditionalEssencePools()) {
      if (!pool.modifiesBasePool()) {
        additionalPool += pool.getAdditionalPersonalPool(traitMap, magicCollection);
      }
    }
    return getStandardPersonalPool() + additionalPool;
  }

  @Override
  public int getStandardPersonalPool() {
    int personal = getUnmodifiedPersonalPool();
    for (IAdditionalEssencePool pool : additionalRules.getAdditionalEssencePools()) {
      if (pool.modifiesBasePool()) {
        personal += pool.getAdditionalPersonalPool(traitMap, magicCollection);
      }
    }
    return personal - Math.max(0, getAttunementExpenditures() - getUnmodifiedPeripheralPool());
  }

  @Override
  public int getUnmodifiedPersonalPool() {
    return getPool(essenceTemplate.getPersonalTraits(getWillpower(), getVirtues(), getEssence()));
  }

  @Override
  public int getFullPeripheralPool() {
    int additionalPool = 0;
    for (IAdditionalEssencePool pool : additionalRules.getAdditionalEssencePools()) {
      additionalPool += pool.getAdditionalPeripheralPool(traitMap, magicCollection);
    }
    return getUnmodifiedPeripheralPool() + additionalPool;
  }

  @Override
  public int getExtendedPeripheralPool() {
    int additionalPool = 0;
    for (IAdditionalEssencePool pool : additionalRules.getAdditionalEssencePools()) {
      if (!pool.modifiesBasePool()) {
        additionalPool += pool.getAdditionalPeripheralPool(traitMap, magicCollection);
      }
    }
    return getStandardPeripheralPool() + additionalPool;
  }

  @Override
  public int getStandardPeripheralPool() {
    int peripheral = getUnmodifiedPeripheralPool();
    for (IAdditionalEssencePool pool : additionalRules.getAdditionalEssencePools()) {
      if (pool.modifiesBasePool()) {
        peripheral += pool.getAdditionalPeripheralPool(traitMap, magicCollection);
      }
    }
    return Math.max(0, peripheral - getAttunementExpenditures());
  }

  @Override
  public int getUnmodifiedPeripheralPool() {
    return getPool(essenceTemplate.getPeripheralTraits(getWillpower(), getVirtues(), getEssence()));
  }

  @Override
  public int getOverdrivePool() {
    int overdrive = 0;
    for (ICharm charm : charmConfiguration.getLearnedCharms()) {
      if (charm.hasAttribute(IExtendedCharmData.OVERDRIVE_ATTRIBUTE)) {
        int pool = 10;
        String value = charm.getAttributeValue(IExtendedCharmData.OVERDRIVE_ATTRIBUTE);
        if (value != null) {
          try {
            pool = Integer.valueOf(value);
          } catch (NumberFormatException e) {
            System.err.println("WARNING: could not parse Overdrive value for charm " + charm.getId() + "; ignoring keyword");
            continue;
          }
        }
        overdrive += pool;
        if (overdrive >= 25) {
          return 25;
        }
      }
    }
    return overdrive;
  }

  @Override
  public IdentifiedInteger[] getComplexPools() {
    Map<String, Integer> complexPools = new HashMap<>();
    for (IAdditionalEssencePool pool : additionalRules.getAdditionalEssencePools()) {
      for (IdentifiedInteger complexPool : pool.getAdditionalComplexPools(traitMap, magicCollection)) {
        String id = complexPool.getId();
        int value = complexPool.getValue();
        if (complexPools.containsKey(id)) {
          value += complexPools.get(id);
        }
        complexPools.put(id, value);
      }
    }
    IdentifiedInteger[] r = new IdentifiedInteger[complexPools.size()];
    int i = 0;
    for (Entry<String, Integer> entry : complexPools.entrySet()) {
      r[i] = new IdentifiedInteger(entry.getKey(), entry.getValue());
      i++;
    }
    return r;
  }

  @Override
  public int getAttunementExpenditures() {
    List<IEssencePoolModifier> modifiers = context.getAllRegistered(IEssencePoolModifier.class);
    int expenditure = 0;
    for (IEssencePoolModifier modifier : modifiers) {
      expenditure += modifier.getMotesExpended();
    }
    return expenditure;
  }

  private GenericTrait[] getVirtues() {
    return new GenericTrait[]{traitMap.getTrait(VirtueType.Compassion), traitMap.getTrait(VirtueType.Conviction),
            traitMap.getTrait(VirtueType.Temperance), traitMap.getTrait(VirtueType.Valor)};
  }

  private GenericTrait getWillpower() {
    return traitMap.getTrait(OtherTraitType.Willpower);
  }

  private GenericTrait getEssence() {
    return traitMap.getTrait(OtherTraitType.Essence);
  }

  private int getPool(FactorizedTrait[] factorizedTraits) {
    return new FactorizedTraitSumCalculator().calculateSum(factorizedTraits);
  }
}