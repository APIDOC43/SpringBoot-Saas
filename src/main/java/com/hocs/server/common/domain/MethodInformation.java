package com.hocs.server.common.domain;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.base.Objects;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Getter
@Data
@NoArgsConstructor
public class MethodInformation {
	private String signature;// ex: sayHello(int int)


	public MethodInformation(MethodDeclaration method) {
		this.signature = method.getSignature().asString();
	}

	public MethodInformation(String signature) {
		this.signature = signature;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MethodInformation that = (MethodInformation) o;
		return Objects.equal(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(signature);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("signature", signature)
			.toString();
	}
}
