package genseq.gui;

import java.awt.AWTEventMulticaster;
import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

/**
 * ValueSlider - A class to implement a slider.
 * 
 * A slider is like AWT's Scrollbar, except that Scrollbar places large
 * values at the bottom and low values at the top of its visible range.
 * This is a headache to deal with. This class allows us to define low
 * values and high values at either end of a Scrollbar.
 * 
 */
class ValueSlider extends Panel implements AdjustmentListener, Adjustable {

	private Scrollbar scrollbar;
	private AdjustmentListener al;
	private boolean inverted;
	
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	
	public ValueSlider(int orientation, int value, int vis, int min, int max) {
		inverted = (min > max);
		
		if (inverted)
			scrollbar = new Scrollbar(orientation, value, vis, max, min);
		else
			scrollbar = new Scrollbar(orientation, value, vis, min, max);
		
		scrollbar.addAdjustmentListener(this);
		
		setLayout(new BorderLayout());
		add(scrollbar, BorderLayout.CENTER);
	}
	
	public ValueSlider(int orientation) {
		this(orientation, 0, 1, 0, 100);
	}
	
	public ValueSlider() {
		this(Scrollbar.VERTICAL);
	}
	
	private int invertValue(int value) {
		int min = scrollbar.getMinimum();
		int max = scrollbar.getMaximum() - scrollbar.getVisibleAmount();
		
		if (value < min)
			value = min;
		if (value > max)
			value = max;
		
		return max + min - value;
	}
	
	@Override
	public void addAdjustmentListener(AdjustmentListener al) {
		this.al = AWTEventMulticaster.add(this.al, al);
	}

	@Override
	public int getBlockIncrement() {
		return scrollbar.getBlockIncrement();
	}

	@Override
	public int getMaximum() {
		return scrollbar.getMaximum();
	}

	@Override
	public int getMinimum() {
		return scrollbar.getMinimum();
	}

	@Override
	public int getOrientation() {
		return scrollbar.getOrientation();
	}

	@Override
	public int getUnitIncrement() {
		return scrollbar.getUnitIncrement();
	}

	@Override
	public int getValue() {
		if (inverted)
			return invertValue(scrollbar.getValue());
		else
			return scrollbar.getValue();
	}

	@Override
	public int getVisibleAmount() {
		return scrollbar.getVisibleAmount();
	}

	@Override
	public void removeAdjustmentListener(AdjustmentListener al) {
		this.al = AWTEventMulticaster.remove(this.al, al);
	}

	@Override
	public void setBlockIncrement(int arg0) {
		scrollbar.setBlockIncrement(arg0);
	}

	@Override
	public void setMaximum(int arg0) {
		scrollbar.setMaximum(arg0);
	}

	@Override
	public void setMinimum(int arg0) {
		scrollbar.setMinimum(arg0);
	}

	@Override
	public void setUnitIncrement(int arg0) {
		scrollbar.setUnitIncrement(arg0);
	}

	@Override
	public void setValue(int arg0) {
		if (inverted)
			scrollbar.setValue(invertValue(arg0));
		else
			scrollbar.setValue(arg0);
	}

	@Override
	public void setVisibleAmount(int arg0) {
		scrollbar.setVisibleAmount(arg0);
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		if (inverted) {
			AdjustmentEvent invertedEvent = new AdjustmentEvent(this, arg0.getID(), arg0.getAdjustmentType(), invertValue(arg0.getValue()));

			processAdjustmentEvent(invertedEvent);
		}
		else processAdjustmentEvent(arg0);
	}
	
	public void processAdjustmentEvent(AdjustmentEvent ae) {
		if (null != al)
			al.adjustmentValueChanged(ae);
	}
	
}