/**
 * Copyright (C) 2016, Antony Holmes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of copyright holder nor the names of its contributors 
 *     may be used to endorse or promote products derived from this software 
 *     without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.columbia.rdf.matcalc.toolbox.conversion;

import javax.swing.Box;

import org.jebtk.modern.UI;
import org.jebtk.modern.button.CheckBox;
import org.jebtk.modern.button.ModernButtonGroup;
import org.jebtk.modern.button.ModernCheckSwitch;
import org.jebtk.modern.button.ModernRadioButton;
import org.jebtk.modern.dialog.ModernDialogHelpWindow;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.panel.VBox;
import org.jebtk.modern.window.ModernWindow;
import org.jebtk.modern.window.WindowWidgetFocusEvents;

/**
 * The class PatternDiscoveryDialog.
 */
public class GenesDialog extends ModernDialogHelpWindow implements ModernClickListener {
	
	/**
	 * The constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The check reset.
	 */
	private CheckBox mCheckSplit = 
			new ModernCheckSwitch("Split hyphens");
	
	private ModernRadioButton mRadioConvFromHuman = 
			new ModernRadioButton("Human");
	
	private ModernRadioButton mRadioConvFromMouse = 
			new ModernRadioButton("Mouse");
	
	private ModernRadioButton mRadioConvToHuman = 
			new ModernRadioButton("Human");
	
	private ModernRadioButton mRadioConvToMouse = 
			new ModernRadioButton("Mouse");
	

	private CheckBox mCheckSymbol =
			new ModernCheckSwitch("Symbol", true);
	
	private CheckBox mCheckEntrez =
			new ModernCheckSwitch("Entrez ID");
	
	private CheckBox mCheckRefSeq =
			new ModernCheckSwitch("RefSeq ID");
	
	private CheckBox mCheckEnsemblTranscript =
			new ModernCheckSwitch("Ensembl Transcript");
	
	private CheckBox mCheckEnsemblGene =
			new ModernCheckSwitch("Ensembl Gene");
	
	private CheckBox mCheckChr =
			new ModernCheckSwitch("Chromosome");
	
	private CheckBox mCheckStrand =
			new ModernCheckSwitch("Strand");
	
	private CheckBox mCheckConversions =
			new ModernCheckSwitch("Conversions", true);


	/**
	 * Instantiates a new pattern discovery dialog.
	 *
	 * @param parent the parent
	 * @param matrix the matrix
	 * @param groups the groups
	 */
	public GenesDialog(ModernWindow parent) {
		super(parent, "genes.help.url");

		setTitle("Genes");
		
		setup();

		createUi();
	}

	/**
	 * Setup.
	 */
	private void setup() {
		addWindowListener(new WindowWidgetFocusEvents(mOkButton));
		
		new ModernButtonGroup(mRadioConvFromHuman, mRadioConvFromMouse);
		new ModernButtonGroup(mRadioConvToHuman, mRadioConvToMouse);
		
		mRadioConvFromHuman.doClick();
		mRadioConvToHuman.doClick();
		
		setSize(540, 540);
		
		UI.centerWindowToScreen(this);
	}

	/**
	 * Creates the ui.
	 */
	private final void createUi() {
		//this.getContentPane().add(new JLabel("Change " + getProductDetails().getProductName() + " settings", JLabel.LEFT), BorderLayout.PAGE_START);

		Box box = VBox.create();
		
		sectionHeader("Input", box);
		
		box.add(mRadioConvFromHuman);
		//box.add(UI.createVGap(5));
		box.add(mRadioConvFromMouse);
		box.add(UI.createVGap(5));
		box.add(mCheckSplit);

		midSectionHeader("Output", box);
		
		box.add(mRadioConvToHuman);
		//box.add(UI.createVGap(5));
		box.add(mRadioConvToMouse);
		box.add(UI.createVGap(10));
		box.add(mCheckConversions);
		box.add(mCheckSymbol);
		//box2.add(UI.createVGap(5));
		box.add(mCheckEntrez);
		//box2.add(UI.createVGap(5));
		box.add(mCheckRefSeq);
		//box2.add(UI.createVGap(5));
		box.add(mCheckEnsemblTranscript);
		//box2.add(UI.createVGap(5));
		box.add(mCheckEnsemblGene);
		//box2.add(UI.createVGap(5));
		box.add(mCheckChr);
		//box2.add(UI.createVGap(5));
		box.add(mCheckStrand);
		//box2.add(UI.createVGap(5));
		
		setDialogCardContent(box);
	}

	public boolean getOutputSymbols() {
		return mCheckSymbol.isSelected();
	}

	public boolean getConvFromHuman() {
		return mRadioConvFromHuman.isSelected();
	}
	
	public boolean getConvFromMouse() {
		return mRadioConvFromMouse.isSelected();
	}
	
	public boolean getConvToHuman() {
		return mRadioConvToHuman.isSelected();
	}
	
	public boolean getConvToMouse() {
		return mRadioConvToMouse.isSelected();
	}

	public boolean getOutputChr() {
		return mCheckChr.isSelected();
	}
	
	public boolean getOutputStrand() {
		return mCheckStrand.isSelected();
	}

	public boolean getOutputEntrez() {
		return mCheckEntrez.isSelected();
	}

	public boolean getOutputRefSeq() {
		return mCheckRefSeq.isSelected();
	}

	public boolean getOutputEnsemblGene() {
		return mCheckEnsemblGene.isSelected();
	}
	
	public boolean getOutputEnsemblTranscript() {
		return mCheckEnsemblTranscript.isSelected();
	}

	public boolean getSplit() {
		return mCheckSplit.isSelected();
	}

	public boolean getOutputConversions() {
		return mCheckConversions.isSelected();
	}
}
