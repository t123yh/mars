package mars.lua;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JsePlatform;

import mars.Globals;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstructionFormat;
import mars.mips.instructions.InstructionSet;
import mars.mips.instructions.SimulationCode;

public class LuaBinding extends TwoArgFunction {

	private org.luaj.vm2.Globals luaGlobals;
	
	public LuaBinding() {
		luaGlobals = JsePlatform.standardGlobals();
		luaGlobals.load(this);
	}

	public org.luaj.vm2.Globals getGlobals() {
		return luaGlobals;
	}

	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		env.set("getgpr", new LuaFunc_getgpr());
		env.set("setgpr", new LuaFunc_setgpr());
		env.set("getpc", new LuaFunc_getpc());
		env.set("loadb", new LuaFunc_loadb());
		env.set("loadh", new LuaFunc_loadh());
		env.set("loadw", new LuaFunc_loadw());
		env.set("storeb", new LuaFunc_storeb());
		env.set("storeh", new LuaFunc_storeh());
		env.set("storew", new LuaFunc_storew());
		env.set("branch", new LuaFunc_branch());
		env.set("jump", new LuaFunc_jump());
		env.set("register_instruction", new LuaFunc_register_instruction());
		return LuaValue.NIL;
	}

	// register functions
	static class LuaFunc_getgpr extends OneArgFunction {
		public LuaValue call(LuaValue lvNum) {
			int num = lvNum.checkint();
			int val = RegisterFile.getValue(num);
			return LuaValue.valueOf(val);
		}
	}

	static class LuaFunc_setgpr extends TwoArgFunction {
		public LuaValue call(LuaValue lvNum, LuaValue lvVal) {
			int num = lvNum.checkint();
			int val = lvVal.checkint();
			RegisterFile.updateRegister(num, val);
			return LuaValue.NIL;
		}
	}

	static class LuaFunc_getpc extends ZeroArgFunction {
		public LuaValue call() {
			return LuaNumber.valueOf(RegisterFile.getProgramCounter());
		}
	}

	// load functions
	static class LuaFunc_loadb extends OneArgFunction {
		public LuaValue call(LuaValue lvAddr) {
			int addr = lvAddr.checkint();
			int val;
			try {
				val = Globals.memory.getByte(addr);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("loadb(): address error: %#x%n", addr);
				return LuaValue.NIL;
			}
		}
	}

	static class LuaFunc_loadh extends OneArgFunction {
		public LuaValue call(LuaValue lvAddr) {
			int addr = lvAddr.checkint();
			int val;
			try {
				val = Globals.memory.getHalf(addr);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("loadh(): address error: %#x%n", addr);
				return LuaValue.NIL;
			}
		}
	}

	static class LuaFunc_loadw extends OneArgFunction {
		public LuaValue call(LuaValue lvAddr) {
			int addr = lvAddr.checkint();
			int val;
			try {
				val = Globals.memory.getWord(addr);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("loadw(): address error: %#x%n", addr);
				return LuaValue.NIL;
			}
		}
	}

	// store functions
	static class LuaFunc_storeb extends TwoArgFunction {
		public LuaValue call(LuaValue lvAddr, LuaValue lvVal) {
			int addr = lvAddr.checkint();
			int val = lvVal.checkint();
			try {
				val = Globals.memory.setByte(addr, val);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("storeb(): address error: %#x%n", addr);
			}
			return LuaValue.NIL;
		}
	}

	static class LuaFunc_storeh extends TwoArgFunction {
		public LuaValue call(LuaValue lvAddr, LuaValue lvVal) {
			int addr = lvAddr.checkint();
			int val = lvVal.checkint();
			try {
				val = Globals.memory.setHalf(addr, val);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("storeh(): address error: %#x%n", addr);
			}
			return LuaValue.NIL;
		}
	}

	static class LuaFunc_storew extends TwoArgFunction {
		public LuaValue call(LuaValue lvAddr, LuaValue lvVal) {
			int addr = lvAddr.checkint();
			int val = lvVal.checkint();
			try {
				val = Globals.memory.setWord(addr, val);
				return LuaValue.valueOf(val);
			} catch (AddressErrorException e) {
				System.err.printf("storew(): address error: %#x%n", addr);
			}
			return LuaValue.NIL;
		}
	}

	static class LuaFunc_branch extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue lvOffset) {
			int offset = lvOffset.checkint();
			InstructionSet.processBranch(offset);
			return LuaValue.NIL;
		}
	}

	static class LuaFunc_jump extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue lvAddr) {
			int addr = lvAddr.checkint();
			InstructionSet.processJump(addr);
			return LuaValue.NIL;
		}
	}

	static class LuaFunc_register_instruction extends VarArgFunction {
		public Varargs onInvoke(Varargs args) {
			String template = args.arg(1).checkjstring();
			String formatStr = args.arg(2).checkjstring();
			String encoding = args.arg(3).checkjstring();
			LuaFunction func = args.arg(4).checkfunction();

			BasicInstructionFormat format = null;
			switch (formatStr) {
			case "R":
				format = BasicInstructionFormat.R_FORMAT;
				break;
			case "I":
				format = BasicInstructionFormat.I_FORMAT;
				break;
			case "I_BRANCH":
				format = BasicInstructionFormat.I_BRANCH_FORMAT;
				break;
			case "J":
				format = BasicInstructionFormat.J_FORMAT;
				break;
			default:
				// TODO error: invalid instruction format
			}

			SimulationCode code = new SimulationCode() {
				@Override
				public void simulate(ProgramStatement statement) throws ProcessingException {
					int[] operands = statement.getOperands();
					int n = operands.length;
					LuaValue[] args = new LuaValue[n];
					for (int i=0; i<n; i++) {
						args[i] = LuaNumber.valueOf(operands[i]);
					}
					func.invoke(args);
				}
			};

			Globals.instructionSet.registerInstruction(template, format, encoding, code);

			return LuaValue.NIL;
		}
	}

}