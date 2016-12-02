package commoninterface.neat.nn.core.learning;

import commoninterface.neat.nn.core.Learnable;
import commoninterface.neat.nn.core.LearningEnvironment;
import commoninterface.neat.nn.core.NeuralNet;

/**
 * @author MSimmerson
 *
 *         Describes the learning environment for the NEAT networks
 */
public class GALearnable implements Learnable {
	private static final long serialVersionUID = -3347038621830818372L;
	private LearningEnvironment env;

	public GALearnable(LearningEnvironment env) {
		this.env = env;
	}

	@Override
	public void teach(NeuralNet net) {
		// does nothing
		throw new UnsupportedOperationException("teach operation not supported in NEAT");
	}

	@Override
	public LearningEnvironment learningEnvironment() {
		return (this.env);
	}

}
