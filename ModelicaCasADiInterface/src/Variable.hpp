#ifndef _MODELICACASADI_VAR
#define _MODELICACASADI_VAR

#include <iostream>
#include <map>

#include "symbolic/casadi.hpp"
#include "boost/flyweight.hpp"

#include "types/VariableType.hpp"
#include "Printable.hpp"
namespace ModelicaCasADi
{
/** 
 * Abstract class for Variables, using symolic MX. A variable holds data 
 * so that it can represent a Modelica or Optimica variable. This data
 * consists of attributes and enum variables that tells the variable's
 * primitive data type and its causality and variability. 
 * 
 * A variable can also hold a VariableType that contains information about 
 * its default attributes or the attributes of its user defined type. 
 */
class Variable : public Printable {
    public:
        typedef std::string AttributeKey; 
        typedef CasADi::MX AttributeValue;
    protected:
        typedef boost::flyweights::flyweight<std::string> AttributeKeyInternal;
        typedef std::map<AttributeKeyInternal,AttributeValue> attributeMap;
    public:
        enum Type {
            REAL,
            INTEGER,
            BOOLEAN,
            STRING
        };
        enum Causality {
            INPUT,
            OUTPUT,
            INTERNAL
        };
        enum Variability {
            CONTINUOUS,
            DISCRETE,
            PARAMETER,
            CONSTANT
        };
        Variable();
        /**
         * The Variable class should not be used, use subclasses such 
         * as RealVariable instead.
         * @param A symbolic MX.
         * @param An entry of the enum Causality
         * @param An entry of the enum Variability
         * @param A pointer to a VariableType, default is NULL. 
         */
        Variable(CasADi::MX var, Causality causality,
                Variability variability, 
                VariableType* = NULL);
        
        /** @return True if this variable is an alias */
        bool isAlias() const;
        /** @return True if this variable is negated */
        bool isNegated() const;
        /** @param Bool negated . Only possible for Alias variables*/
        void setNegated(bool negated);
        /** @param Sets an alias for this variable, making this an alias variable */
        void setAlias(Variable* var);
        /** @return This variable's alias, or NULL */
        Variable* getAlias() const;
        
        
        /**
         * @return The string name of this Variable 
         */
        std::string getName() const;
        
        /**
         * @return A MX
         */        
        const CasADi::MX getVar() const;
        /**
         * @return An enum for the primitive data type.
         */ 
        virtual const Type getType() const;
        /**
         * @return An enum for the causality
         */
        const Causality getCausality() const;
        /**
         * @return An enum for the variability.
         */
        const Variability getVariability() const;
        /**
         * Returns the Variable's declared type. This may be one of Modelica's
         * built in types such as Real, which holds Real's default attributes, 
         * or it may be a user defined type.
         * @return A pointer to a VariableType. 
         */
        VariableType* getDeclaredType() const;
        /**
         * Sets the declared type
         * @param A pointer to a VariableType
         */
        void setDeclaredType(VariableType* declaredType);
        
        /** 
         * Looks at local attributes, then at attributes for is declared type,
         * OR at the attributes of its alias if this is an alias variable. 
         * Returns NULL if not present.
         * @param An AttributeKey
         * @return A pointer to an AttributeValue 
         */
        virtual AttributeValue* getAttribute(AttributeKey key);
        /** 
         * A check whether a certain attribute is set in this variable,
         * OR in its alias if this is an alias variable. 
         * @return A bool.  
         */
        bool hasAttributeSet(AttributeKey key) const; 
        /** 
         * Sets an attribute in the local Variable's attribute map, 
         * or it propagates the attribute to its alias if this is an
         * alias variable. 
         * @param An AttributeKey
         * @param An AttributeValue
         */
        void setAttribute(AttributeKey key, AttributeValue val);
        
        /** Allows the use of the operator << to print this class to a stream, through Printable */
        virtual void print(std::ostream& os) const;
        
    protected:
        Variable* myModelVariable; /// If this Variable is a alias, this is its corresponding model variable. 
        bool negated;
        VariableType* declaredType;
        CasADi::MX var;
        attributeMap attributes;
        AttributeValue* getAttributeForAlias(AttributeKey key);
        AttributeKey keyForAlias(AttributeKey key) const;
        void setAttributeForAlias(AttributeKey key, AttributeValue val);
    private:
        Causality causality;
        Variability variability;
};
inline bool Variable::isAlias() const { return myModelVariable != NULL; }
inline bool Variable::isNegated() const { return negated; }
inline void Variable::setNegated(bool negated) { 
    if (!isAlias()) {
        throw std::runtime_error("Only alias variables may be negated");
    }
    this->negated = negated; 
}
inline void Variable::setAlias(Variable* modelVariable) { this->myModelVariable = modelVariable; }
inline Variable* Variable::getAlias() const { return myModelVariable; }
inline std::string Variable::getName() const { return var.getName(); }
inline const Variable::Type Variable::getType() const { throw std::runtime_error("Variable does not have a type"); }
inline void Variable::setDeclaredType(VariableType* declaredType) { this->declaredType = declaredType; }
inline VariableType* Variable::getDeclaredType() const { return declaredType; }
inline const CasADi::MX Variable::getVar() const { return var; }
inline const Variable::Causality Variable::getCausality() const { return causality; }
inline const Variable::Variability Variable::getVariability() const { return variability; }
}; // End namespace
#endif
