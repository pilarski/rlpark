package rltoys.algorithms.representations.ltu.networks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rltoys.algorithms.representations.ltu.units.LTU;
import rltoys.math.vector.BinaryVector;
import rltoys.math.vector.implementations.BVector;
import zephyr.plugin.core.api.monitoring.annotations.IgnoreMonitor;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;

@Monitor
public class RandomNetwork implements Serializable {
  private static final long serialVersionUID = 8140259178658376161L;
  final public int outputSize;
  final public int inputSize;
  @Monitor(level = 4)
  protected final BVector output;
  @Monitor(level = 4)
  protected final LTU[] ltus;
  @IgnoreMonitor
  protected final List<LTU>[] connectedLTUs;
  protected int nbConnection = 0;
  protected int nbActive = 0;

  @SuppressWarnings("unchecked")
  public RandomNetwork(int inputSize, int outputSize) {
    this.outputSize = outputSize;
    this.inputSize = inputSize;
    connectedLTUs = new List[inputSize];
    ltus = new LTU[outputSize];
    output = new BVector(outputSize);
  }

  public void addLTU(LTU ltu) {
    removeLTU(ltus[ltu.index()]);
    ltus[ltu.index()] = ltu;
    Set<Integer> ltuInputs = ltu.inputs();
    for (int input : ltuInputs) {
      if (connectedLTUs[input] == null)
        connectedLTUs[input] = new ArrayList<LTU>();
      connectedLTUs[input].add(ltu);
    }
    addLTUStat(ltu);
  }

  private void addLTUStat(LTU ltu) {
    nbConnection += ltu.inputs().size();
  }

  private void removeLTUStat(LTU ltu) {
    nbConnection -= ltu.inputs().size();
  }

  public void removeLTU(LTU ltu) {
    if (ltu == null)
      return;
    assert ltus[ltu.index()] != null;
    removeLTUStat(ltu);
    ltus[ltu.index()] = null;
    for (int input : ltu.inputs())
      if (connectedLTUs[input] != null)
        connectedLTUs[input].remove(ltu);
  }

  protected Set<LTU> computeLTUSum(BinaryVector obs) {
    Set<LTU> updated = new HashSet<LTU>();
    for (int activeInput : obs.activeIndexes()) {
      List<LTU> connected = connectedLTUs[activeInput];
      if (connected == null)
        continue;
      for (LTU ltu : connected) {
        updated.add(ltu);
        ltu.setActiveInput(activeInput);
      }
    }
    return updated;
  }

  public BVector project(BinaryVector obs) {
    updateActiveLTUs(obs);
    nbActive = output.nonZeroElements();
    return output.copy();
  }

  protected void updateActiveLTUs(BinaryVector obs) {
    output.clear();
    Set<LTU> updated = computeLTUSum(obs);
    updateActiveLTUs(updated);
  }

  private void updateActiveLTUs(Collection<LTU> toUpdate) {
    for (LTU ltu : toUpdate) {
      ltu.update();
      if (ltu.isActive())
        output.setOn(ltu.index());
    }
  }

  public LTU ltu(int i) {
    return ltus[i];
  }

  public List<LTU> parents(int index) {
    return connectedLTUs[index] != null ? connectedLTUs[index] : new ArrayList<LTU>();
  }
}
