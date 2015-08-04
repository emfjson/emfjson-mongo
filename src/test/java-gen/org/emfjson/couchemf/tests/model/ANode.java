/**
 */
package org.emfjson.couchemf.tests.model;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ANode</b></em>'.
 * <!-- end-user-doc -->
 * <p/>
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.emfjson.couchemf.tests.model.ANode#getBNode <em>BNode</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ANode extends Node {
    /**
     * The cached value of the '{@link #getBNode() <em>BNode</em>}' reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getBNode()
     */
    protected BNode bNode;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    protected ANode() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ModelPackage.Literals.ANODE;
    }

    /**
     * Returns the value of the '<em><b>BNode</b></em>' reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>BNode</em>' reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>BNode</em>' reference.
     * @generated
     * @see #setBNode(BNode)
     */
    public BNode getBNode() {
        if (bNode != null && bNode.eIsProxy()) {
            InternalEObject oldBNode = (InternalEObject) bNode;
            bNode = (BNode) eResolveProxy(oldBNode);
            if (bNode != oldBNode) {
                if (eNotificationRequired())
                    eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.ANODE__BNODE, oldBNode, bNode));
            }
        }
        return bNode;
    }

    /**
     * Sets the value of the '{@link org.emfjson.couchemf.tests.model.ANode#getBNode <em>BNode</em>}' reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>BNode</em>' reference.
     * @generated
     * @see #getBNode()
     */
    public void setBNode(BNode newBNode) {
        BNode oldBNode = bNode;
        bNode = newBNode;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.ANODE__BNODE, oldBNode, bNode));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public BNode basicGetBNode() {
        return bNode;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case ModelPackage.ANODE__BNODE:
                if (resolve) return getBNode();
                return basicGetBNode();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case ModelPackage.ANODE__BNODE:
                setBNode((BNode) newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case ModelPackage.ANODE__BNODE:
                setBNode((BNode) null);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case ModelPackage.ANODE__BNODE:
                return bNode != null;
        }
        return super.eIsSet(featureID);
    }

} // ANode
