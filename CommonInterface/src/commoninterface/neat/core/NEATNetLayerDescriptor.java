/*
 * Created on Sep 29, 2004
 *
 */
package commoninterface.neat.core;

import commoninterface.neat.nn.core.ActivationFunction;
import commoninterface.neat.nn.core.NeuralNetLayerDescriptor;

/**
 * @author MSimmerson
 *
 */
public class NEATNetLayerDescriptor implements NeuralNetLayerDescriptor {
	private static final long serialVersionUID = -2259756985332431767L;
	private int layerSize;
	private int layerInputSize;
	private int layerId;
	private ActivationFunction function;
	private boolean isOutputLayer = false;
	private boolean nodesSelfRecurrent;
	
	public NEATNetLayerDescriptor(ActivationFunction function, int layerSize, int layerInputsSize, int id) {
		this.layerSize = layerSize;
		this.layerId = id;
		this.function = function;
		this.layerInputSize = layerInputsSize;
	}
	
	public NEATNetLayerDescriptor(ActivationFunction function, int layerSize, int layerInputsSize, int id, boolean opLayer, boolean selfRecurrent) {
		this(function, layerSize, layerInputsSize, id);
		this.isOutputLayer = opLayer;
		this.nodesSelfRecurrent = selfRecurrent;
	}

	@Override
	public int layerSize() {
		return this.layerSize;
	}

	@Override
	public int layerId() {
		return (this.layerId);
	}

	@Override
	public ActivationFunction activationFunction() {
		return (this.function);
	}

	@Override
	public int inputsIntoLayer() {
		return (this.layerInputSize);
	}

	@Override
	public boolean isOutputLayer() {
		return (this.isOutputLayer);
	}

	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.nn.core.NeuralNetLayerDescriptor#nodesSelfRecurrent()
	 */
	@Override
	public boolean nodesSelfRecurrent() {
		return (this.nodesSelfRecurrent);
	}
}
