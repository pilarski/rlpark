package rltoys.demons;

import rltoys.algorithms.learning.predictions.LinearLearner;
import rltoys.algorithms.learning.predictions.Predictor;
import rltoys.algorithms.learning.predictions.td.GTD;
import rltoys.algorithms.representations.acting.Policy;
import rltoys.algorithms.representations.actions.Action;
import rltoys.demons.functions.ConstantGamma;
import rltoys.demons.functions.ConstantOutcomeFunction;
import rltoys.demons.functions.GammaFunction;
import rltoys.demons.functions.OutcomeFunction;
import rltoys.demons.functions.RewardFunction;
import rltoys.math.vector.RealVector;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;

public class PredictionOffPolicyDemon implements Demon {
  private static final long serialVersionUID = 2103050204892958885L;
  private final RewardFunction rewardFunction;
  @Monitor
  private final GTD gtd;
  @Monitor
  protected final Policy target;
  protected final Policy behaviour;
  @Monitor
  private double rho_t;
  private final OutcomeFunction outcomeFunction;
  private final GammaFunction gammaFunction;

  public PredictionOffPolicyDemon(Policy target, Policy behaviour, GTD gtd, RewardFunction rewardFunction) {
    this(target, behaviour, gtd, rewardFunction, new ConstantGamma(gtd.gamma()), new ConstantOutcomeFunction(0));
  }

  public PredictionOffPolicyDemon(Policy target, Policy behaviour, GTD gtd, RewardFunction rewardFunction,
      GammaFunction gammaFunction, OutcomeFunction outcomeFunction) {
    this.rewardFunction = rewardFunction;
    this.gammaFunction = gammaFunction;
    this.outcomeFunction = outcomeFunction;
    this.gtd = gtd;
    this.target = target;
    this.behaviour = behaviour;
  }

  @Override
  public void update(RealVector x_t, Action a_t, RealVector x_tp1) {
    rho_t = a_t != null ? target.pi(x_t, a_t) / behaviour.pi(x_t, a_t) : 0;
    gtd.update(rho_t, x_t, x_tp1, rewardFunction.reward(), gammaFunction.gamma(), outcomeFunction.outcome());
  }

  public RewardFunction rewardFunction() {
    return rewardFunction;
  }

  public Predictor predicter() {
    return gtd;
  }

  public Policy targetPolicy() {
    return target;
  }

  @Override
  public LinearLearner learner() {
    return gtd;
  }
}
