#ifndef _MODELICACASADI_CONSTRAINT
#define _MODELICACASADI_CONSTRAINT
#include <symbolic/casadi.hpp>
#include <iostream>
#include <Printable.hpp>
namespace ModelicaCasADi{
class Constraint : public Printable {
    public:
        enum Type {
            EQ,
            LEQ,
            GEQ
        };
        Constraint();
        /**
         * Create a constraint from MX for the left and right hand side, 
         * and a relation type (<, >, ==).
         * @param An MX
         * @param An MX
         * @param A Type enum
         */
        Constraint(CasADi::MX lhs, CasADi::MX rhs, Type ct);
        /** @return An MX */                   
        const CasADi::MX getLhs() const;
        /** @return An MX */
        const CasADi::MX getRhs() const;
        /**
         * Returns the residual of the constraint as: right-hand-side - left-hand-side.
         * @return An MX
         */
        const CasADi::MX getResidual() const; 
        /** @return An enum Type */
        Type getType() const;
        /** Allows the use of the operator << to print this class to a stream, through Printable */
        virtual void print(std::ostream& os) const;
    private:
        CasADi::MX lhs;
        CasADi::MX rhs;
        Type ct;
};
inline Constraint::Constraint() {}
inline const CasADi::MX Constraint::getLhs() const{ return lhs; }
inline const CasADi::MX Constraint::getRhs() const { return rhs; }
inline const CasADi::MX Constraint::getResidual() const{ return rhs-lhs; }
inline Constraint::Type Constraint::getType() const{ return ct; }
}; // End namespace
#endif
