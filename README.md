# Prediction of expertise level from BPMN models

Fully automatic, un-obtrusive, tool independent approach to predict the expertise level of modeler, based on the BPMN artifacts they draw.
 
Current implementation uses  a  feed-forward  neural  network,  with  one  hidden  layer  comprising 50  neurons.  The  input  layer  contains  10  neurons,  one  for  each  feature  of  the vectorial representation of the model. The output layer contains 1 neuron, whose value distinguishes between the two classes (i.e., _novice_ or _expert_).

The  multilayer perceptron was trained with a learning rate of 0.3 and a learning momentum of 0.2. Additionally, we set the number of training epochs to 500.
